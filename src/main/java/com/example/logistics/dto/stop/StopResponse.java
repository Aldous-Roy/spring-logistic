package com.example.logistics.dto.stop;

import com.example.logistics.entity.enums.DeliveryStatus;
import com.example.logistics.entity.enums.PodRequirement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StopResponse(
        String orderId,
        String routeCode,
        Integer sequenceNumber,
        String customerName,
        String customerPhone,
        String deliveryAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime timeWindowStart,
        LocalDateTime timeWindowEnd,
        BigDecimal packageWeightKg,
        BigDecimal packageVolumeCbms,
        Integer serviceTimeMins,
        DeliveryStatus status,
        PodRequirement requiredPodType,
        LocalDateTime estimatedArrivalTime,
        String failedReasonNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
