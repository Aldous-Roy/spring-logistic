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

        RouteOptimizationService.RoutePlan routePlan = routeOptimizationService.optimize(orders);
        List<DeliveryOrder> orderedOrders = routePlan.orderedOrders().isEmpty() ? orders : routePlan.orderedOrders();

        int sequence = 1;
        for (DeliveryOrder order : orderedOrders) {
            order.setRoute(route);
            order.setSequenceNumber(sequence++);
            order.setStatus(DeliveryStatus.ROUTED);
        }
        route.setTotalStops(orderedOrders.size());
        route.setCompletedStops(0);
        route.setRoutePolyline(routePlan.routePolyline());
        route.setTotalDistanceKm(routePlan.totalDistanceKm());
        route.setEstimatedDurationMins(routePlan.estimatedDurationMins());
        if (route.getStatus() == RouteStatus.CREATED) {
            route.setStatus(RouteStatus.ASSIGNED);
        }
        orderRepository.saveAll(orderedOrders);
        routeRepository.save(route);
        return toResponse(route);
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
        return toResponse(routeRepository.save(route));
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
