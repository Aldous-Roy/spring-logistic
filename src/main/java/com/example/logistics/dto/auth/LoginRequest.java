package com.example.logistics.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String employeeId,
        @NotBlank String password
) {
}
