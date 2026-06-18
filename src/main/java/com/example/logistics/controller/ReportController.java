package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.report.ManagerAnalyticsResponse;
import com.example.logistics.service.DeliveryOrderService;
import com.example.logistics.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final DeliveryOrderService orderService;
    private final DriverService driverService;

    @GetMapping("/manager")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<ManagerAnalyticsResponse>> getManagerAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(
            new ManagerAnalyticsResponse(
                orderService.countTotalStopsToday(),
                orderService.countCompletedStopsToday(),
                orderService.countFailedStopsToday(),
                orderService.countPendingStopsToday(),
                driverService.countActiveDrivers()
            ), 200
        ));
    }
}
