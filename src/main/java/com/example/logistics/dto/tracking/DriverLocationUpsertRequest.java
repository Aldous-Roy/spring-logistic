package com.example.logistics.dto.tracking;

import jakarta.validation.constraints.NotNull;

public record DriverLocationUpsertRequest(
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
