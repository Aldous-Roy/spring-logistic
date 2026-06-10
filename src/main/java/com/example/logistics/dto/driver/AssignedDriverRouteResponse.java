package com.example.logistics.dto.driver;

import com.example.logistics.entity.enums.RouteStatus;

import java.time.LocalDate;
import java.util.UUID;

public record AssignedDriverRouteResponse(
        UUID routeId,
        String routeCode,
        LocalDate routeDate,
        RouteStatus status,
        Integer stopCount
) {
}
