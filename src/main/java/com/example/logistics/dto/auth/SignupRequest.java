package com.example.logistics.dto.auth;

import com.example.logistics.entity.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank String name,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull UserRole role
) {
}
