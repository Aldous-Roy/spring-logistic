package com.example.logistics.dto.stop;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReassignStopRequest(
        @NotNull UUID routeId,
        @NotNull Integer sequenceNumber
) {
}
