package com.example.logistics.dto.route;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RouteCreateRequest(
        @NotBlank @Pattern(regexp = "^[A-Z0-9_-]{3,50}$") String routeCode,
        @NotNull LocalDate routeDate,
        BigDecimal totalDistanceKm,
        Integer estimatedDurationMins,
        String routePolyline
) {
}
