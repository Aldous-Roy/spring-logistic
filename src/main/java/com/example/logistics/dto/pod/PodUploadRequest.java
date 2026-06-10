package com.example.logistics.dto.pod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record PodUploadRequest(
        @NotBlank String deliveryId,
        @NotNull MultipartFile image,
        String customerSignature
) {
}
