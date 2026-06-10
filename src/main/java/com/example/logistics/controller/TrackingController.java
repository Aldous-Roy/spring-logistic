package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.tracking.DriverLocationResponse;
import com.example.logistics.dto.tracking.DriverLocationUpsertRequest;
import com.example.logistics.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    /**
     * API: POST /api/tracking/location
     * Method: updateLocation
     * Postman Request:
     * {
     *   "latitude": 12.9715987,
     *   "longitude": 77.594566
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21",
     *     "latitude": 12.9715987,
     *     "longitude": 77.594566,
     *     "timestamp": "2026-06-10T11:00:00"
     *   }
     * }
     */
    @PostMapping("/location")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<DriverLocationResponse>> updateLocation(@Valid @RequestBody DriverLocationUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.upsert(request), 200));
    }

    /**
     * API: GET /api/tracking/location/{driverId}
     * Method: latest
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21",
     *     "latitude": 12.9715987,
     *     "longitude": 77.594566,
     *     "timestamp": "2026-06-10T11:00:00"
     *   }
     * }
     */
    @GetMapping("/location/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER','DISPATCHER')")
    public ResponseEntity<ApiResponse<DriverLocationResponse>> latest(@PathVariable UUID driverId) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.latest(driverId), 200));
    }
}
