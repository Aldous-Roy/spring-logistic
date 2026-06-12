package com.example.logistics.dto.route;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ResequenceOrdersRequest(
        @NotEmpty List<String> orderIds
) {
}
