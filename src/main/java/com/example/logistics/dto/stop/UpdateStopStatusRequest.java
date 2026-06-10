package com.example.logistics.dto.stop;

import com.example.logistics.entity.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStopStatusRequest(
        @NotNull DeliveryStatus status,
        String failedReasonNotes
) {
}
