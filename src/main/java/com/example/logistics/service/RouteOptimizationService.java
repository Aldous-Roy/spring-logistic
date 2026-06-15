package com.example.logistics.service;

import com.example.logistics.entity.DeliveryOrder;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class RouteOptimizationService {

    private static final double AVERAGE_SPEED_KMH = 35.0d;

    public RoutePlan optimize(List<DeliveryOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return new RoutePlan(List.of(), null, BigDecimal.ZERO, 0);
        }

        List<DeliveryOrder> ordered = optimizeWithJsprit(orders);
        if (ordered.isEmpty()) {
            ordered = fallbackNearestNeighbor(orders);
        }

        List<RoutePoint> points = ordered.stream()
                .map(order -> new RoutePoint(order.getLatitude().doubleValue(), order.getLongitude().doubleValue()))
                .toList();

        String encodedPolyline = PolylineEncoder.encode(points);
        double distanceKm = calculateDistanceKm(points);
        int estimatedMinutes = estimateMinutes(distanceKm, ordered);

        return new RoutePlan(ordered, encodedPolyline, BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP), estimatedMinutes);
    }

    public RoutePlan summarize(List<DeliveryOrder> orderedOrders) {
        if (orderedOrders == null || orderedOrders.isEmpty()) {
            return new RoutePlan(List.of(), null, BigDecimal.ZERO, 0);
        }

        List<RoutePoint> points = orderedOrders.stream()
                .map(order -> new RoutePoint(order.getLatitude().doubleValue(), order.getLongitude().doubleValue()))
                .toList();

        String encodedPolyline = PolylineEncoder.encode(points);
        double distanceKm = calculateDistanceKm(points);
        int estimatedMinutes = estimateMinutes(distanceKm, orderedOrders);

        return new RoutePlan(orderedOrders, encodedPolyline, BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP), estimatedMinutes);
    }

    private List<DeliveryOrder> optimizeWithJsprit(List<DeliveryOrder> orders) {
        RoutePoint depot = centroid(orders);

        VehicleRoutingProblem.Builder problemBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("route-optimizer")
                .setStartLocation(Location.newInstance(depot.longitude(), depot.latitude()))
                .setReturnToDepot(false)
                .build();
        problemBuilder.addVehicle(vehicle);

        Map<String, DeliveryOrder> ordersById = new LinkedHashMap<>();
        for (DeliveryOrder order : orders) {
            ordersById.put(order.getOrderId(), order);
            com.graphhopper.jsprit.core.problem.job.Service service = com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(order.getOrderId())
                    .setLocation(Location.newInstance(
                            order.getLongitude().doubleValue(),
                            order.getLatitude().doubleValue()))
                    .build();
            problemBuilder.addJob(service);
        }

        VehicleRoutingProblem problem = problemBuilder.build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution best = solutions.stream()
                .min(Comparator.comparingDouble(VehicleRoutingProblemSolution::getCost))
                .orElse(null);
        if (best == null || best.getRoutes().isEmpty()) {
            return List.of();
        }

        VehicleRoute route = best.getRoutes().iterator().next();
        List<DeliveryOrder> ordered = new ArrayList<>();
        for (TourActivity activity : route.getActivities()) {
            if (activity instanceof TourActivity.JobActivity jobActivity) {
                DeliveryOrder order = ordersById.get(jobActivity.getJob().getId());
                if (order != null) {
                    ordered.add(order);
                }
            }
        }

        if (ordered.size() != orders.size()) {
            return List.of();
        }
        return ordered;
    }

    private List<DeliveryOrder> fallbackNearestNeighbor(List<DeliveryOrder> orders) {
        RoutePoint depot = centroid(orders);
        List<DeliveryOrder> remaining = new ArrayList<>(orders);
        List<DeliveryOrder> ordered = new ArrayList<>(orders.size());
        RoutePoint current = depot;

        while (!remaining.isEmpty()) {
            final RoutePoint currentPoint = current;
            DeliveryOrder next = remaining.stream()
                    .min(Comparator.comparingDouble(order ->
                            distanceKm(currentPoint.latitude(), currentPoint.longitude(),
                                    order.getLatitude().doubleValue(), order.getLongitude().doubleValue())))
                    .orElseThrow();
            ordered.add(next);
            current = new RoutePoint(next.getLatitude().doubleValue(), next.getLongitude().doubleValue());
            remaining.remove(next);
        }

        return ordered;
    }

    private RoutePoint centroid(List<DeliveryOrder> orders) {
        double lat = 0.0d;
        double lon = 0.0d;
        for (DeliveryOrder order : orders) {
            lat += order.getLatitude().doubleValue();
            lon += order.getLongitude().doubleValue();
        }
        return new RoutePoint(lat / orders.size(), lon / orders.size());
    }

    private double calculateDistanceKm(List<RoutePoint> points) {
        if (points.size() < 2) {
            return 0.0d;
        }
        double distance = 0.0d;
        for (int i = 1; i < points.size(); i++) {
            RoutePoint previous = points.get(i - 1);
            RoutePoint current = points.get(i);
            distance += distanceKm(previous.latitude(), previous.longitude(), current.latitude(), current.longitude());
        }
        return distance;
    }

    private int estimateMinutes(double distanceKm, List<DeliveryOrder> ordered) {
        int serviceMinutes = ordered.stream().mapToInt(order -> order.getServiceTimeMins() == null ? 0 : order.getServiceTimeMins()).sum();
        double travelMinutes = (distanceKm / AVERAGE_SPEED_KMH) * 60.0d;
        return (int) Math.ceil(serviceMinutes + travelMinutes);
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    public record RoutePlan(
            List<DeliveryOrder> orderedOrders,
            String routePolyline,
            BigDecimal totalDistanceKm,
            Integer estimatedDurationMins
    ) {
    }

    record RoutePoint(double latitude, double longitude) {
    }
}
