package com.example.logistics.dto.driver;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        UUID driverId,
        LocalDateTime checkedInAt,
        LocalDateTime checkedOutAt,
        boolean active
) {
}
