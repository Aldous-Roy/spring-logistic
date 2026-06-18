package com.example.logistics.dto.report;

public record ManagerAnalyticsResponse(
        long totalStops,
        long completedStops,
        long exceptionStops,
        long pendingStops,
        long activeFleet
) {
}
