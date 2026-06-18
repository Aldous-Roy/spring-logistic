package com.example.logistics.entity;

import com.example.logistics.entity.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "drivers", indexes = {
        @Index(name = "idx_drivers_employee_id", columnList = "employee_id", unique = true)
})
public class DriverProfile extends AuditableEntity {

    @jakarta.persistence.Id
    @UuidGenerator
    @Column(name = "driver_id", nullable = false, updatable = false)
    private UUID driverId;

    @Column(name = "employee_id", nullable = false, unique = true, length = 100)
    private String employeeId;

    @Column(name = "dispatcher_id", length = 100)
    private String dispatcherId;

    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "max_package_capacity", nullable = false)
    private Integer maxPackageCapacity = 50;

    @Column(name = "max_weight_capacity_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxWeightCapacityKg = new BigDecimal("300.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType = VehicleType.VAN;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_profile_setup", nullable = false)
    private boolean profileSetup = false;

    @Column(name = "performance_score", nullable = false)
    private Integer performanceScore = 100;

    @Column(name = "total_completed_orders", nullable = false)
    private Long totalCompletedOrders = 0L;

    @Column(name = "total_failed_orders", nullable = false)
    private Long totalFailedOrders = 0L;
}


