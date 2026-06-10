package com.example.logistics.dto.auth;

import com.example.logistics.entity.enums.UserRole;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        String employeeId,
        String name,
        UserRole role
) {
}
