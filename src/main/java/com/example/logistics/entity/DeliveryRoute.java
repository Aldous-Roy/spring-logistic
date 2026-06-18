package com.example.logistics.entity;

import com.example.logistics.entity.enums.RouteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.example.logistics.entity.enums.VehicleType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "routes", indexes = {
        @Index(name = "idx_routes_route_code", columnList = "route_code", unique = true),
        @Index(name = "idx_routes_route_date", columnList = "route_date"),
        @Index(name = "idx_routes_driver_id", columnList = "driver_id"),
        @Index(name = "idx_routes_status", columnList = "status")
})
public class DeliveryRoute extends AuditableEntity {

    @jakarta.persistence.Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID routeId;

    @Column(name = "route_id", nullable = false, unique = true, updatable = false)
    private UUID routeUuid;

    @Column(name = "assigned_driver_id")
    private UUID assignedDriverId;

    @Column(name = "completed_stops", nullable = false)
    private Integer completedStops = 0;

    @Column(name = "total_stops", nullable = false)
    private Integer totalStops = 0;

    @Column(name = "route_code", nullable = false, unique = true, length = 100)
    private String routeCode;

    @Column(name = "route_date", nullable = false)
    private LocalDate routeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RouteStatus status = RouteStatus.CREATED;

    @Column(name = "driver_id")
    private UUID driverId;

    @Column(name = "total_distance_km", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalDistanceKm = BigDecimal.ZERO;

    @Column(name = "estimated_duration_mins", nullable = false)
    private Integer estimatedDurationMins = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_vehicle_type", length = 20)
    private VehicleType requiredVehicleType;

    @Column(name = "route_polyline", columnDefinition = "text")
    private String routePolyline;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;

    @Column(name = "actual_end_at")
    private LocalDateTime actualEndAt;
}
