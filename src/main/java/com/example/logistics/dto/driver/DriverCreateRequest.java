package com.example.logistics.dto.driver;

import com.example.logistics.entity.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DriverCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phoneNumber,
        @NotBlank String password,
        Integer maxPackageCapacity,
        BigDecimal maxWeightCapacityKg,
        @NotNull Boolean active,
        VehicleType vehicleType
) {
}
