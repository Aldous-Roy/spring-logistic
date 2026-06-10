package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.dashboard.DashboardSummaryResponse;
import com.example.logistics.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * API: GET /api/dashboard/summary
     * Method: summary
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "totalUsers": 2,
     *     "totalDrivers": 1,
     *     "activeDrivers": 1,
     *     "totalRoutes": 3,
     *     "routesByStatus": {
     *       "DRAFT": 1,
     *       "PUBLISHED": 1,
     *       "ACTIVE": 1,
     *       "COMPLETED": 0,
     *       "CANCELLED": 0
     *     },
     *     "ordersByStatus": {
     *       "PENDING": 1,
     *       "ROUTED": 2,
     *       "OUT_FOR_DELIVERY": 1,
     *       "DELIVERED": 4,
     *       "FAILED": 0
     *     },
     *     "deliveredToday": 4,
     *     "pendingPods": 2
     *   }
     * }
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.summary(), 200));
    }
}
