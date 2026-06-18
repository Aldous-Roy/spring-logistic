package com.example.logistics.service;

import com.example.logistics.dto.common.PageResponse;
import com.example.logistics.dto.route.AssignDriverRequest;
import com.example.logistics.dto.route.AssignOrdersRequest;
import com.example.logistics.dto.route.RouteCreateRequest;
import com.example.logistics.dto.route.RouteResponse;
import com.example.logistics.dto.route.ResequenceOrdersRequest;
import com.example.logistics.entity.DeliveryOrder;
import com.example.logistics.entity.DeliveryRoute;
import com.example.logistics.entity.DriverProfile;
import com.example.logistics.entity.enums.DeliveryStatus;
import com.example.logistics.entity.enums.RouteStatus;
import com.example.logistics.exception.InvalidOperationException;
import com.example.logistics.exception.ResourceNotFoundException;
import com.example.logistics.repository.DeliveryOrderRepository;
import com.example.logistics.repository.DeliveryRouteRepository;
import com.example.logistics.repository.DriverProfileRepository;
import com.example.logistics.repository.DriverAttendanceRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final DeliveryRouteRepository routeRepository;
    private final DeliveryOrderRepository orderRepository;
    private final DriverProfileRepository driverRepository;
    private final DriverAttendanceRepository attendanceRepository;
    private final RouteOptimizationService routeOptimizationService;

    @Transactional
    public RouteResponse create(RouteCreateRequest request) {
        DeliveryRoute route = new DeliveryRoute();
        UUID routeUuid = UUID.randomUUID();
        route.setRouteUuid(routeUuid);
        route.setRouteCode(request.routeCode());
        route.setRouteDate(request.routeDate());
        route.setTotalDistanceKm(request.totalDistanceKm() == null ? BigDecimal.ZERO : request.totalDistanceKm());
        route.setEstimatedDurationMins(request.estimatedDurationMins() == null ? 0 : request.estimatedDurationMins());
        route.setRoutePolyline(request.routePolyline());
        route.setStatus(RouteStatus.CREATED);
        route.setTotalStops(0);
        route.setCompletedStops(0);
        return toResponse(routeRepository.save(route));
    }

    public PageResponse<RouteResponse> list(Pageable pageable, String search) {
        Page<DeliveryRoute> page = StringUtils.hasText(search)
                ? routeRepository.findByRouteCodeContainingIgnoreCase(search, pageable)
                : routeRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public RouteResponse getById(UUID id) {
        return toResponse(findRoute(id));
    }

    public DeliveryRoute findRouteByCode(String routeCode) {
        return routeRepository.findByRouteCode(routeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found for code: " + routeCode));
    }

    @Transactional
    public RouteResponse assignDriver(UUID routeId, AssignDriverRequest request) {
        DeliveryRoute route = findRoute(routeId);
        DriverProfile driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.driverId()));
        route.setDriverId(driver.getDriverId());
        route.setAssignedDriverId(driver.getDriverId());
        if (route.getStatus() == RouteStatus.CREATED) {
            route.setStatus(RouteStatus.ASSIGNED);
        }
        return toResponse(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse assignOrders(UUID routeId, AssignOrdersRequest request) {
        DeliveryRoute route = findRoute(routeId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot assign orders to a closed route");
        }
        List<DeliveryOrder> orders = orderRepository.findAllById(request.orderIds());
        if (orders.size() != request.orderIds().size()) {
            throw new ResourceNotFoundException("One or more delivery orders were not found");
        }

        List<RouteOptimizationService.RoutePlan> routePlans = routeOptimizationService.optimize(orders);

        if (routePlans.isEmpty() || routePlans.get(0).orderedOrders().isEmpty()) {
            // Fallback if optimization fails
            int sequence = 1;
            for (DeliveryOrder order : orders) {
                order.setRoute(route);
                order.setSequenceNumber(sequence++);
                order.setStatus(DeliveryStatus.ROUTED);
            }
            route.setTotalStops(orders.size());
            route.setCompletedStops(0);
            if (route.getStatus() == RouteStatus.CREATED) {
                route.setStatus(RouteStatus.ASSIGNED);
            }
            orderRepository.saveAll(orders);
            routeRepository.save(route);
            return toResponse(route);
        }

        // Apply the first RoutePlan to the existing route
        RouteOptimizationService.RoutePlan primaryPlan = routePlans.get(0);
        applyPlanToRoute(route, primaryPlan);

        // For any additional split routes, create new DeliveryRoute entities automatically
        for (int i = 1; i < routePlans.size(); i++) {
            RouteOptimizationService.RoutePlan extraPlan = routePlans.get(i);
            
            DeliveryRoute newRoute = new DeliveryRoute();
            newRoute.setRouteUuid(UUID.randomUUID());
            newRoute.setRouteCode(route.getRouteCode() + "-SPLIT-" + i);
            newRoute.setRouteDate(route.getRouteDate());
            newRoute.setStatus(RouteStatus.CREATED);
            routeRepository.save(newRoute);
            
            applyPlanToRoute(newRoute, extraPlan);
        }

        return toResponse(route);
    }

    private void applyPlanToRoute(DeliveryRoute route, RouteOptimizationService.RoutePlan plan) {
        int sequence = 1;
        for (DeliveryOrder order : plan.orderedOrders()) {
            order.setRoute(route);
            order.setSequenceNumber(sequence++);
            order.setStatus(DeliveryStatus.ROUTED);
        }
        route.setTotalStops(plan.orderedOrders().size());
        route.setCompletedStops(0);
        route.setRequiredVehicleType(plan.requiredVehicleType());
        route.setRoutePolyline(plan.routePolyline());
        route.setTotalDistanceKm(plan.totalDistanceKm());
        route.setEstimatedDurationMins(plan.estimatedDurationMins());
        if (route.getStatus() == RouteStatus.CREATED) {
            route.setStatus(RouteStatus.ASSIGNED);
        }
        orderRepository.saveAll(plan.orderedOrders());
        routeRepository.save(route);
    }

    @Transactional
    public RouteResponse resequenceOrders(UUID routeId, ResequenceOrdersRequest request) {
        DeliveryRoute route = findRoute(routeId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot resequence orders on a closed route");
        }

        List<DeliveryOrder> currentOrders = orderRepository.findByRouteOrderBySequenceNumberAscCreatedAtAsc(route);
        if (currentOrders.size() != request.orderIds().size()) {
            throw new InvalidOperationException("Order list must include every stop assigned to the route");
        }
        if (new HashSet<>(request.orderIds()).size() != request.orderIds().size()) {
            throw new InvalidOperationException("Order list contains duplicate orderIds");
        }

        Map<String, DeliveryOrder> ordersById = new LinkedHashMap<>();
        for (DeliveryOrder order : currentOrders) {
            ordersById.put(order.getOrderId(), order);
        }

        for (String orderId : request.orderIds()) {
            if (!ordersById.containsKey(orderId)) {
                throw new ResourceNotFoundException("Order not found on this route: " + orderId);
            }
        }

        int sequence = 1;
        for (String orderId : request.orderIds()) {
            ordersById.get(orderId).setSequenceNumber(sequence++);
        }
        List<DeliveryOrder> finalOrders = request.orderIds().stream()
                .map(ordersById::get)
                .toList();
        RouteOptimizationService.RoutePlan routePlan = routeOptimizationService.summarize(finalOrders);
        route.setRoutePolyline(routePlan.routePolyline());
        route.setTotalDistanceKm(routePlan.totalDistanceKm());
        route.setEstimatedDurationMins(routePlan.estimatedDurationMins());
        orderRepository.saveAll(finalOrders);
        routeRepository.save(route);
        return toResponse(route);
    }

    @Transactional
    public RouteResponse publish(UUID routeId) {
        DeliveryRoute route = findRoute(routeId);
        if (route.getStatus() != RouteStatus.CREATED && route.getStatus() != RouteStatus.ASSIGNED) {
            throw new InvalidOperationException("Only created or assigned routes can be published");
        }
        route.setStatus(RouteStatus.ASSIGNED);
        route.setPublishedAt(LocalDateTime.now());
        return toResponse(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse activate(UUID routeId) {
        DeliveryRoute route = findRoute(routeId);
        if (route.getStatus() != RouteStatus.ASSIGNED) {
            throw new InvalidOperationException("Only assigned routes can be activated");
        }
        route.setStatus(RouteStatus.IN_PROGRESS);
        route.setActualStartAt(LocalDateTime.now());
        return toResponse(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse complete(UUID routeId) {
        DeliveryRoute route = findRoute(routeId);
        if (route.getStatus() != RouteStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Only in-progress routes can be completed");
        }
        route.setStatus(RouteStatus.COMPLETED);
        route.setActualEndAt(LocalDateTime.now());
        
        updateDriverPerformanceOnRouteCompletion(route);
        
        return toResponse(routeRepository.save(route));
    }

    private void updateDriverPerformanceOnRouteCompletion(DeliveryRoute route) {
        if (route.getDriverId() == null) {
            return;
        }
        driverRepository.findById(route.getDriverId()).ifPresent(driver -> {
            List<DeliveryRoute> completedRoutes = routeRepository.findByDriverIdAndStatus(driver.getDriverId(), RouteStatus.COMPLETED);
            long totalCompletedOrders = 0;
            long totalFailedOrders = 0;
            long totalOnTimeOrders = 0;
            long totalAssignedOrders = 0;

            for (DeliveryRoute completedRoute : completedRoutes) {
                List<DeliveryOrder> orders = orderRepository.findByRouteOrderBySequenceNumberAscCreatedAtAsc(completedRoute);
                for (DeliveryOrder order : orders) {
                    totalAssignedOrders++;
                    if (order.getStatus() == DeliveryStatus.DELIVERED) {
                        totalCompletedOrders++;
                        if (order.getEstimatedArrivalTime() != null && order.getUpdatedAt() != null) {
                            if (!order.getUpdatedAt().isAfter(order.getEstimatedArrivalTime())) {
                                totalOnTimeOrders++;
                            }
                        } else {
                            totalOnTimeOrders++;
                        }
                    } else if (order.getStatus() == DeliveryStatus.FAILED) {
                        totalFailedOrders++;
                    }
                }
            }

            double successRate = totalAssignedOrders > 0 ? (double) totalCompletedOrders / totalAssignedOrders : 1.0;
            double onTimeRate = totalCompletedOrders > 0 ? (double) totalOnTimeOrders / totalCompletedOrders : 1.0;

            double calculatedScore = (successRate * 60.0) + (onTimeRate * 40.0);
            int finalScore = (int) Math.round(calculatedScore);
            finalScore = Math.max(0, Math.min(100, finalScore));

            driver.setTotalCompletedOrders(totalCompletedOrders);
            driver.setTotalFailedOrders(totalFailedOrders);
            driver.setPerformanceScore(finalScore);
            driverRepository.save(driver);
        });
    }

    @Transactional
    public List<RouteResponse> autoAllocateRoutes() {
        LocalDate today = LocalDate.now();
        List<DeliveryRoute> unassignedRoutes = routeRepository.findByDriverIdIsNullAndRouteDate(today);
        if (unassignedRoutes.isEmpty()) {
            return List.of();
        }

        List<com.example.logistics.entity.DriverAttendance> activeAttendances = attendanceRepository.findByActiveTrueOrderByCheckedInAtAsc();
        if (activeAttendances.isEmpty()) {
            return List.of();
        }

        List<DriverProfile> activeDrivers = new java.util.ArrayList<>();
        for (com.example.logistics.entity.DriverAttendance att : activeAttendances) {
            driverRepository.findById(att.getDriverId()).ifPresent(activeDrivers::add);
        }

        activeDrivers.sort((d1, d2) -> d2.getPerformanceScore().compareTo(d1.getPerformanceScore()));

        List<UUID> assignedDriverIds = routeRepository.findAll().stream()
                .filter(r -> today.equals(r.getRouteDate()) && r.getDriverId() != null)
                .map(DeliveryRoute::getDriverId)
                .toList();

        List<DriverProfile> availableDrivers = activeDrivers.stream()
                .filter(d -> !assignedDriverIds.contains(d.getDriverId()))
                .collect(java.util.stream.Collectors.toList());

        List<DeliveryRoute> allocatedRoutes = new java.util.ArrayList<>();
        List<DeliveryRoute> sortedRoutes = new java.util.ArrayList<>(unassignedRoutes);
        sortedRoutes.sort((r1, r2) -> r2.getTotalStops().compareTo(r1.getTotalStops()));

        for (DeliveryRoute route : sortedRoutes) {
            DriverProfile matchedDriver = null;
            for (int i = 0; i < availableDrivers.size(); i++) {
                DriverProfile d = availableDrivers.get(i);
                // Match required vehicle type if it's set by Jsprit
                if (route.getRequiredVehicleType() == null || route.getRequiredVehicleType() == d.getVehicleType()) {
                    matchedDriver = d;
                    availableDrivers.remove(i);
                    break;
                }
            }

            if (matchedDriver != null) {
                route.setDriverId(matchedDriver.getDriverId());
                route.setAssignedDriverId(matchedDriver.getDriverId());
                if (route.getStatus() == RouteStatus.CREATED) {
                    route.setStatus(RouteStatus.ASSIGNED);
                }
                allocatedRoutes.add(routeRepository.save(route));
            }
        }

        return allocatedRoutes.stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<RouteResponse> autoGroupAndAllocateTodaysOrders() {
        LocalDate today = LocalDate.now();
        List<DeliveryOrder> pendingOrders = orderRepository.findByDeliveryDateAndStatus(today, DeliveryStatus.PENDING);
        if (pendingOrders.isEmpty()) {
            return List.of();
        }

        List<RouteOptimizationService.RoutePlan> routePlans = routeOptimizationService.optimize(pendingOrders);
        List<DeliveryRoute> createdRoutes = new java.util.ArrayList<>();

        int i = 1;
        for (RouteOptimizationService.RoutePlan plan : routePlans) {
            if (plan.orderedOrders().isEmpty()) continue;

            DeliveryRoute newRoute = new DeliveryRoute();
            newRoute.setRouteUuid(UUID.randomUUID());
            newRoute.setRouteCode("AUTO-" + today.toString().replace("-", "") + "-" + i);
            newRoute.setRouteDate(today);
            newRoute.setStatus(RouteStatus.CREATED);
            routeRepository.save(newRoute);

            applyPlanToRoute(newRoute, plan);
            createdRoutes.add(newRoute);
            i++;
        }

        return createdRoutes.stream().map(this::toResponse).toList();
    }

    @Transactional
    public RouteResponse cancel(UUID routeId) {
        DeliveryRoute route = findRoute(routeId);
        if (route.getStatus() == RouteStatus.COMPLETED) {
            throw new InvalidOperationException("Completed routes cannot be cancelled");
        }
        route.setStatus(RouteStatus.CANCELLED);
        route.setActualEndAt(LocalDateTime.now());
        return toResponse(routeRepository.save(route));
    }

    public DeliveryRoute findRoute(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + id));
    }

    private void ensureStatus(DeliveryRoute route, RouteStatus expected, String message) {
        if (route.getStatus() != expected) {
            throw new InvalidOperationException(message);
        }
    }

    private RouteResponse toResponse(DeliveryRoute route) {
        return new RouteResponse(
                route.getRouteId(),
                route.getRouteCode(),
                route.getRouteDate(),
                route.getStatus(),
                route.getDriverId(),
                route.getRequiredVehicleType(),
                route.getTotalDistanceKm(),
                route.getEstimatedDurationMins(),
                route.getRoutePolyline(),
                route.getCreatedAt(),
                route.getPublishedAt(),
                route.getActualStartAt(),
                route.getActualEndAt(),
                route.getUpdatedAt()
        );
    }
}
