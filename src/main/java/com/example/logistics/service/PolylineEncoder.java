package com.example.logistics.service;

import java.util.List;

final class PolylineEncoder {

    private PolylineEncoder() {
    }

    static String encode(List<RouteOptimizationService.RoutePoint> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        long prevLat = 0;
        long prevLng = 0;

        for (RouteOptimizationService.RoutePoint point : points) {
            long lat = Math.round(point.latitude() * 1e5);
            long lng = Math.round(point.longitude() * 1e5);
            encodeValue(lat - prevLat, result);
            encodeValue(lng - prevLng, result);
            prevLat = lat;
            prevLng = lng;
        }

        return result.toString();
    }

    private static void encodeValue(long value, StringBuilder result) {
        value = value < 0 ? ~(value << 1) : value << 1;
        while (value >= 0x20) {
            result.append((char) ((0x20 | (value & 0x1f)) + 63));
            value >>= 5;
        }
        result.append((char) (value + 63));
    }
}
