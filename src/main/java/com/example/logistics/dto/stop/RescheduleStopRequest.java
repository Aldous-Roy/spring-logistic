package com.example.logistics.dto.stop;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RescheduleStopRequest(
        @NotNull LocalDate deliveryDate
) {
}
