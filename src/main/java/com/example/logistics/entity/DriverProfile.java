package com.example.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
