package com.example.logistics.dto.route;

import com.example.logistics.entity.enums.RouteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RouteResponse(
        UUID routeId,
        String routeCode,
        LocalDate routeDate,
        RouteStatus status,
        UUID driverId,
        BigDecimal totalDistanceKm,
        Integer estimatedDurationMins,
        String routePolyline,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        LocalDateTime updatedAt
) {
}
