package com.example.logistics.dto.dashboard;

import com.example.logistics.entity.enums.DeliveryStatus;
import com.example.logistics.entity.enums.RouteStatus;

import java.util.Map;

public record DashboardSummaryResponse(
        long totalUsers,
        long totalDrivers,
        long activeDrivers,
        long totalRoutes,
        Map<RouteStatus, Long> routesByStatus,
        Map<DeliveryStatus, Long> ordersByStatus,
        long deliveredToday,
        long pendingPods
) {
}
