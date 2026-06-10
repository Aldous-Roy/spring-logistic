package com.example.logistics.entity;

import com.example.logistics.entity.enums.DeliveryStatus;
import com.example.logistics.entity.enums.PodRequirement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "delivery_orders", indexes = {
        @Index(name = "idx_delivery_orders_route_id", columnList = "route_id"),
        @Index(name = "idx_delivery_orders_status", columnList = "status"),
        @Index(name = "idx_delivery_orders_sequence_number", columnList = "sequence_number")
})
public class DeliveryOrder extends AuditableEntity {

    @jakarta.persistence.Id
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private DeliveryRoute route;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Column(name = "delivery_address", nullable = false, length = 1000)
    private String deliveryAddress;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "time_window_start")
    private LocalDateTime timeWindowStart;

    @Column(name = "time_window_end")
    private LocalDateTime timeWindowEnd;

    @Column(name = "package_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal packageWeightKg = new BigDecimal("1.00");

    @Column(name = "package_volume_cbms", nullable = false, precision = 5, scale = 3)
    private BigDecimal packageVolumeCbms = new BigDecimal("0.010");

    @Column(name = "service_time_mins", nullable = false)
    private Integer serviceTimeMins = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_pod_type", nullable = false, length = 30)
    private PodRequirement requiredPodType = PodRequirement.PHOTO_REQUIRED;

    @Column(name = "estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    @Column(name = "failed_reason_notes", columnDefinition = "text")
    private String failedReasonNotes;
}
