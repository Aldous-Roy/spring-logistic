package com.example.logistics.service;

import com.example.logistics.dto.auth.AuthResponse;
import com.example.logistics.dto.auth.ChangePasswordRequest;
import com.example.logistics.dto.auth.LoginRequest;
import com.example.logistics.dto.auth.SignupRequest;
import com.example.logistics.dto.auth.UserResponse;
import com.example.logistics.entity.AppUser;
import com.example.logistics.entity.DriverProfile;
import com.example.logistics.entity.enums.UserRole;
import com.example.logistics.exception.InvalidCredentialsException;
import com.example.logistics.repository.AppUserRepository;
import com.example.logistics.repository.DriverProfileRepository;
import com.example.logistics.security.CustomUserDetails;
import com.example.logistics.security.JwtService;
import com.example.logistics.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final EmployeeIdGenerator employeeIdGenerator;
    private final DriverProfileRepository driverRepository;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        AppUser user = new AppUser();
        String employeeId = employeeIdGenerator.generate();
        user.setEmployeeId(employeeId);
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);
        AppUser saved = userRepository.save(user);
        String token = jwtService.generateToken(new CustomUserDetails(saved));

        UUID driverId = null;
        String firstName = null;
        String lastName = null;
        String phoneNumber = null;
        Integer maxPackageCapacity = null;
        BigDecimal maxWeightCapacityKg = null;
        Boolean profileComplete = null;

        if (saved.getRole() == UserRole.DRIVER) {
            DriverProfile driver = getOrCreateDriverProfile(saved);
            driverId = driver.getDriverId();
            firstName = driver.getFirstName();
            lastName = driver.getLastName();
            phoneNumber = driver.getPhoneNumber();
            maxPackageCapacity = driver.getMaxPackageCapacity();
            maxWeightCapacityKg = driver.getMaxWeightCapacityKg();
            profileComplete = !"0000000000".equals(driver.getPhoneNumber());
        }

        return new AuthResponse(token, "Bearer", jwtProperties.expirationMs(), saved.getEmployeeId(), saved.getName(), saved.getRole(),
                driverId, firstName, lastName, phoneNumber, maxPackageCapacity, maxWeightCapacityKg, profileComplete);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.employeeId(), request.password())
            );
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Invalid employee ID or password");
        }
        AppUser user = userRepository.findByEmployeeId(request.employeeId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid employee ID or password"));
        String token = jwtService.generateToken(new CustomUserDetails(user));

        UUID driverId = null;
        String firstName = null;
        String lastName = null;
        String phoneNumber = null;
        Integer maxPackageCapacity = null;
        BigDecimal maxWeightCapacityKg = null;
        Boolean profileComplete = null;

        if (user.getRole() == UserRole.DRIVER) {
            DriverProfile driver = getOrCreateDriverProfile(user);
            driverId = driver.getDriverId();
            firstName = driver.getFirstName();
            lastName = driver.getLastName();
            phoneNumber = driver.getPhoneNumber();
            maxPackageCapacity = driver.getMaxPackageCapacity();
            maxWeightCapacityKg = driver.getMaxWeightCapacityKg();
            profileComplete = !"0000000000".equals(driver.getPhoneNumber());
        }

        return new AuthResponse(token, "Bearer", jwtProperties.expirationMs(), user.getEmployeeId(), user.getName(), user.getRole(),
                driverId, firstName, lastName, phoneNumber, maxPackageCapacity, maxWeightCapacityKg, profileComplete);
    }

    private DriverProfile getOrCreateDriverProfile(AppUser user) {
        return driverRepository.findByEmployeeId(user.getEmployeeId())
                .orElseGet(() -> {
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
                    return driverRepository.save(driver);
                });
    }

    @Transactional
    public UserResponse changePassword(AppUser currentUser, ChangePasswordRequest request, PasswordEncoder encoder) {
        if (!encoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        currentUser.setPassword(encoder.encode(request.newPassword()));
        AppUser saved = userRepository.save(currentUser);
        return new UserResponse(saved.getId(), saved.getEmployeeId(), saved.getName(), saved.getRole(), saved.isActive());
    }
}
