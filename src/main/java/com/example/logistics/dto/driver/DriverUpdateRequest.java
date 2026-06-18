package com.example.logistics.dto.driver;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record DriverUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phoneNumber,
        Integer maxPackageCapacity,
        BigDecimal maxWeightCapacityKg
) {
}
