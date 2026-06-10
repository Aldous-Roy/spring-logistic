package com.example.logistics.dto.pod;

import java.time.LocalDateTime;
import java.util.UUID;

public record PodUploadResponse(
        UUID id,
        String deliveryId,
        String imageUrl,
        String customerSignature,
        LocalDateTime uploadedAt
) {
}
