package com.example.logistics.dto.driver;

import com.example.logistics.entity.enums.DeliveryStatus;

public record TodayStopResponse(
        String orderId,
        String routeCode,
        Integer sequenceNumber,
        String customerName,
        String deliveryAddress,
        DeliveryStatus status
) {
}
