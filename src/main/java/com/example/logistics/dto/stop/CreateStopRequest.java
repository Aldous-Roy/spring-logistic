package com.example.logistics.dto.stop;

import com.example.logistics.entity.enums.PodRequirement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateStopRequest(
        @NotBlank String orderId,
        String routeCode,
        @NotBlank String customerName,
        @NotBlank String customerPhone,
        @NotBlank String deliveryAddress,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude,
        LocalDateTime timeWindowStart,
        LocalDateTime timeWindowEnd,
        LocalDate deliveryDate,
        BigDecimal packageWeightKg,
        BigDecimal packageVolumeCbms,
        Integer serviceTimeMins,
        PodRequirement requiredPodType
) {
}
