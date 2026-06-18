package com.example.logistics.service;

import com.example.logistics.dto.driver.AssignedDriverRouteResponse;
import com.example.logistics.dto.driver.AttendanceResponse;
import com.example.logistics.dto.driver.DriverCreateRequest;
import com.example.logistics.dto.driver.DriverResponse;
import com.example.logistics.dto.driver.TodayStopResponse;
import com.example.logistics.dto.common.PageResponse;
import com.example.logistics.entity.DeliveryOrder;
import com.example.logistics.entity.DeliveryRoute;
import com.example.logistics.entity.DriverAttendance;
import com.example.logistics.entity.DriverProfile;
import com.example.logistics.entity.AppUser;
import com.example.logistics.entity.enums.DeliveryStatus;
import com.example.logistics.entity.enums.RouteStatus;
import com.example.logistics.entity.enums.UserRole;
import com.example.logistics.exception.ConflictException;
import com.example.logistics.exception.InvalidOperationException;
import com.example.logistics.exception.ResourceNotFoundException;
import com.example.logistics.repository.DeliveryOrderRepository;
import com.example.logistics.repository.DeliveryRouteRepository;
import com.example.logistics.repository.DriverAttendanceRepository;
import com.example.logistics.repository.DriverProfileRepository;
import com.example.logistics.security.CurrentUserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverProfileRepository driverRepository;
    private final DeliveryRouteRepository routeRepository;
    private final DeliveryOrderRepository orderRepository;
    private final DriverAttendanceRepository attendanceRepository;
    private final CurrentUserFacade currentUserFacade;
    private final EmployeeIdGenerator employeeIdGenerator;

    @Transactional
    public DriverResponse upsertDriver(DriverCreateRequest request) {
        DriverProfile driver = new DriverProfile();
        driver.setEmployeeId(employeeIdGenerator.generate());
        driver.setFirstName(request.firstName());
        driver.setLastName(request.lastName());
        driver.setPhoneNumber(request.phoneNumber());
        driver.setMaxPackageCapacity(request.maxPackageCapacity() == null ? 50 : request.maxPackageCapacity());
        driver.setMaxWeightCapacityKg(request.maxWeightCapacityKg() == null ? new BigDecimal("300.00") : request.maxWeightCapacityKg());
        driver.setActive(false);
        DriverProfile saved = driverRepository.save(driver);
        return toResponse(saved);
    }

    public PageResponse<DriverResponse> listDrivers(Pageable pageable, String search) {
        Page<DriverProfile> page = StringUtils.hasText(search)
                ? driverRepository.findByEmployeeIdContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
                        search, search, search, search, pageable)
                : driverRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public AttendanceResponse checkIn() {
        DriverProfile driver = currentDriver();
        attendanceRepository.findByDriverId(driver.getDriverId()).ifPresent(attendance -> {
            if (attendance.isActive()) {
                throw new ConflictException("Driver is already checked in");
            }
        });
        driver.setActive(true);
        driverRepository.save(driver);
        DriverAttendance attendance = attendanceRepository.findByDriverId(driver.getDriverId()).orElseGet(DriverAttendance::new);
        attendance.setDriverId(driver.getDriverId());
        attendance.setCheckedInAt(LocalDateTime.now());
        attendance.setCheckedOutAt(null);
        attendance.setActive(true);
        DriverAttendance saved = attendanceRepository.save(attendance);
        return toAttendanceResponse(saved);
    }

    @Transactional
    public AttendanceResponse checkOut() {
        DriverProfile driver = currentDriver();
        DriverAttendance attendance = attendanceRepository.findByDriverId(driver.getDriverId())
                .orElseThrow(() -> new InvalidOperationException("Driver has not checked in"));
        if (!attendance.isActive()) {
            throw new ConflictException("Driver is already checked out");
        }
        driver.setActive(false);
        driverRepository.save(driver);
        attendance.setCheckedOutAt(LocalDateTime.now());
        attendance.setActive(false);
        return toAttendanceResponse(attendanceRepository.save(attendance));
    }

    public AttendanceResponse currentAttendance() {
        DriverProfile driver = currentDriver();
        return attendanceRepository.findByDriverId(driver.getDriverId())
                .map(this::toAttendanceResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found for current driver"));
    }

    @Transactional
    public AttendanceResponse toggleBreak(boolean onBreak) {
        DriverProfile driver = currentDriver();
        DriverAttendance attendance = attendanceRepository.findByDriverId(driver.getDriverId())
                .orElseThrow(() -> new InvalidOperationException("Driver has not checked in"));
        if (!attendance.isActive()) {
            throw new InvalidOperationException("Cannot toggle break when not on active shift");
        }
        attendance.setOnBreak(onBreak);
        attendance.setBreakStartedAt(onBreak ? LocalDateTime.now() : null);
        return toAttendanceResponse(attendanceRepository.save(attendance));
    }

    public List<AssignedDriverRouteResponse> assignedRoutes() {
        DriverProfile driver = currentDriver();
        return routeRepository.findByDriverIdOrderByRouteDateAscCreatedAtAsc(driver.getDriverId()).stream()
                .map(route -> new AssignedDriverRouteResponse(
                        route.getRouteId(),
                        route.getRouteCode(),
                        route.getRouteDate(),
                        route.getStatus(),
                        orderRepository.findByRouteOrderBySequenceNumberAscCreatedAtAsc(route).size()
                ))
                .toList();
    }

    public List<TodayStopResponse> todaysStops() {
        DriverProfile driver = currentDriver();
        LocalDate today = LocalDate.now();
        return routeRepository.findByDriverIdAndRouteDate(driver.getDriverId(), today).stream()
                .flatMap(route -> orderRepository.findByRouteOrderBySequenceNumberAscCreatedAtAsc(route).stream())
                .map(order -> new TodayStopResponse(
                        order.getOrderId(),
                        order.getRoute() == null ? null : order.getRoute().getRouteCode(),
                        order.getSequenceNumber(),
                        order.getCustomerName(),
                        order.getDeliveryAddress(),
                        order.getStatus()
                ))
                .toList();
    }

    public DriverProfile currentDriver() {
        AppUser currentUser = currentUserFacade.currentUser();
        if (currentUser.getRole() != UserRole.DRIVER) {
            throw new ResourceNotFoundException("Driver profile not available for non-driver user: " + currentUser.getEmployeeId());
        }
        return driverRepository.findByEmployeeId(currentUser.getEmployeeId())
                .orElseGet(() -> driverRepository.save(createDriverProfile(currentUser)));
    }

    private DriverProfile createDriverProfile(AppUser user) {
        String[] nameParts = user.getName() == null ? new String[0] : user.getName().trim().split("\\s+", 2);
        String firstName = nameParts.length > 0 && !nameParts[0].isBlank() ? nameParts[0] : "Driver";
        String lastName = nameParts.length > 1 && !nameParts[1].isBlank() ? nameParts[1] : "User";

        DriverProfile driver = new DriverProfile();
        driver.setEmployeeId(user.getEmployeeId());
        driver.setFirstName(firstName);
        driver.setLastName(lastName);
        driver.setPhoneNumber("0000000000");
        driver.setMaxPackageCapacity(50);
        driver.setMaxWeightCapacityKg(new BigDecimal("300.00"));
        driver.setActive(false);
        return driver;
    }

    private DriverResponse toResponse(DriverProfile driver) {
        return new DriverResponse(
                driver.getDriverId(),
                driver.getEmployeeId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getPhoneNumber(),
                driver.getMaxPackageCapacity(),
                driver.getMaxWeightCapacityKg(),
                driver.isActive(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }

    private AttendanceResponse toAttendanceResponse(DriverAttendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getDriverId(),
                attendance.getCheckedInAt(),
                attendance.getCheckedOutAt(),
                attendance.isActive(),
                attendance.isOnBreak(),
                attendance.getBreakStartedAt()
        );
    }
}
