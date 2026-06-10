package com.example.logistics.dto.tracking;

import java.time.LocalDateTime;
import java.util.UUID;

public record DriverLocationResponse(
        UUID driverId,
        Double latitude,
        Double longitude,
        LocalDateTime timestamp
) {
}
