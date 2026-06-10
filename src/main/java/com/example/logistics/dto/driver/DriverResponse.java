package com.example.logistics.dto.driver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DriverResponse(
        UUID driverId,
        String employeeId,
        String firstName,
        String lastName,
        String phoneNumber,
        Integer maxPackageCapacity,
        BigDecimal maxWeightCapacityKg,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
