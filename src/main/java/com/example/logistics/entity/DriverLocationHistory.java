package com.example.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "driver_location_history",
    indexes = {
        @Index(name = "idx_driver_location_history_driver_timestamp", columnList = "driver_id, timestamp DESC")
    }
)
@Getter
@Setter
public class DriverLocationHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "route_id")
    private UUID routeId;
}
