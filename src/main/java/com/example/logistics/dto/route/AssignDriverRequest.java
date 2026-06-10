package com.example.logistics.dto.route;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDriverRequest(
        @NotNull UUID driverId
) {
}
