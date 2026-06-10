package com.example.logistics.dto.auth;

import com.example.logistics.entity.enums.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String employeeId,
        String name,
        UserRole role,
        boolean active
) {
}
