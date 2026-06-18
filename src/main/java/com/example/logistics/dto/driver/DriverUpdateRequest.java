package com.example.logistics.dto.driver;

import com.example.logistics.entity.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record DriverUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phoneNumber,
        Integer maxPackageCapacity,
        BigDecimal maxWeightCapacityKg,
        VehicleType vehicleType
) {
}
