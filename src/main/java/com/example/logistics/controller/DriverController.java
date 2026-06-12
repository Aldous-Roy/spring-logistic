package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.common.PageResponse;
import com.example.logistics.dto.driver.AttendanceResponse;
import com.example.logistics.dto.driver.AssignedDriverRouteResponse;
import com.example.logistics.dto.driver.DriverCreateRequest;
import com.example.logistics.dto.driver.DriverResponse;
import com.example.logistics.dto.driver.TodayStopResponse;
import com.example.logistics.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    /**
     * API: POST /api/drivers
     * Method: create
     * Postman Request:
     * {
     *   "firstName": "Ravi",
     *   "lastName": "Kumar",
     *   "phoneNumber": "9876543210",
     *   "maxPackageCapacity": 50,
     *   "maxWeightCapacityKg": 300.00,
     *   "active": false
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21",
     *     "employeeId": "EMP-20260610-AB12CD",
     *     "firstName": "Ravi",
     *     "lastName": "Kumar",
     *     "phoneNumber": "9876543210",
     *     "maxPackageCapacity": 50,
     *     "maxWeightCapacityKg": 300.00,
     *     "active": true
     *   }
     * }
     */
    @PostMapping
//    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<DriverResponse>> create(@Valid @RequestBody DriverCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(driverService.upsertDriver(request), 200));
    }

    /**
     * API: GET /api/drivers
     * Method: list
     * Postman Request:
     * GET /api/drivers?search=Ravi&page=0&size=10&sort=createdAt,desc
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "content": [
     *       {
     *         "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21",
     *         "employeeId": "DRV2001",
     *         "firstName": "Ravi",
     *         "lastName": "Kumar",
     *         "phoneNumber": "9876543210",
     *         "maxPackageCapacity": 50,
     *         "maxWeightCapacityKg": 300.00,
     *         "active": false
     *       }
     *     ],
     *     "pageNumber": 0,
     *     "pageSize": 10,
     *     "totalElements": 1,
     *     "totalPages": 1,
     *     "first": true,
     *     "last": true
     *   }
     * }
     */
    @GetMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<PageResponse<DriverResponse>>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(driverService.listDrivers(pageable, search), 200));
    }

    /**
     * API: POST /api/drivers/check-in
     * Method: checkIn
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "id": "c8d6c1f0-2f0d-4af2-9f2b-3ad43bb2ef15",
     *     "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21",
     *     "checkedInAt": "2026-06-10T09:00:00",
     *     "checkedOutAt": null,
     *     "active": true
     *   }
     * }
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn() {
        return ResponseEntity.ok(ApiResponse.success(driverService.checkIn(), 200));
    }

    /**
     * API: POST /api/drivers/check-out
     * Method: checkOut
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "id": "c8d6c1f0-2f0d-4af2-9f2b-3ad43bb2ef15",
     *     "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21",
     *     "checkedInAt": "2026-06-10T09:00:00",
     *     "checkedOutAt": "2026-06-10T18:00:00",
     *     "active": false
     *   }
     * }
     */
    @PostMapping("/check-out")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut() {
        return ResponseEntity.ok(ApiResponse.success(driverService.checkOut(), 200));
    }

    /**
     * API: GET /api/drivers/attendance/current
     * Method: currentAttendance
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "id": "c8d6c1f0-2f0d-4af2-9f2b-3ad43bb2ef15",
     *     "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21",
     *     "checkedInAt": "2026-06-10T09:00:00",
     *     "checkedOutAt": null,
     *     "active": true
     *   }
     * }
     */
    @GetMapping("/attendance/current")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> currentAttendance() {
        return ResponseEntity.ok(ApiResponse.success(driverService.currentAttendance(), 200));
    }

    /**
     * API: GET /api/drivers/routes
     * Method: assignedRoutes
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": [
     *     {
     *       "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *       "routeCode": "RT-20260610-01",
     *       "routeDate": "2026-06-10",
     *       "status": "PUBLISHED",
     *       "stopCount": 12
     *     }
     *   ]
     * }
     */
    @GetMapping("/routes")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<AssignedDriverRouteResponse>>> assignedRoutes() {
        return ResponseEntity.ok(ApiResponse.success(driverService.assignedRoutes(), 200));
    }

    /**
     * API: GET /api/drivers/stops/today
     * Method: todayStops
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": [
     *     {
     *       "orderId": "ORD-1001",
     *       "routeCode": "RT-20260610-01",
     *       "sequenceNumber": 1,
     *       "customerName": "John Smith",
     *       "deliveryAddress": "12 Main Street",
     *       "status": "ROUTED"
     *     }
     *   ]
     * }
     */
    @GetMapping("/stops/today")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<TodayStopResponse>>> todayStops() {
        return ResponseEntity.ok(ApiResponse.success(driverService.todaysStops(), 200));
    }
}
