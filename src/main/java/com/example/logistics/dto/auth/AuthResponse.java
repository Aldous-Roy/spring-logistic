package com.example.logistics.dto.auth;

import com.example.logistics.entity.enums.UserRole;
import java.math.BigDecimal;
import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        String employeeId,
        String name,
        UserRole role,
        UUID driverId,
        String firstName,
        String lastName,
        String phoneNumber,
        Integer maxPackageCapacity,
        BigDecimal maxWeightCapacityKg,
        Boolean profileComplete
) {
}
