package com.example.logistics.dto.route;

import java.util.List;

public record DriverRouteResponse(
    String id,
    String routeNumber,
    String status,
    List<StopDetail> stops
) {
    public record StopDetail(
        String id,
        int stopNumber,
        String address,
        String customerName,
        int packageCount,
        String status,
        String eta,
        Double latitude,
        Double longitude,
        Integer serviceTimeMins
    ) {}
}
