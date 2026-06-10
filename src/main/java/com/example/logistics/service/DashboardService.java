package com.example.logistics.service;

import com.example.logistics.dto.dashboard.DashboardSummaryResponse;
import com.example.logistics.entity.enums.DeliveryStatus;
import com.example.logistics.entity.enums.RouteStatus;
import com.example.logistics.repository.AppUserRepository;
import com.example.logistics.repository.DeliveryOrderRepository;
import com.example.logistics.repository.DeliveryRouteRepository;
import com.example.logistics.repository.DriverProfileRepository;
import com.example.logistics.repository.PodRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppUserRepository userRepository;
    private final DriverProfileRepository driverRepository;
    private final DeliveryRouteRepository routeRepository;
    private final DeliveryOrderRepository orderRepository;
    private final PodRecordRepository podRepository;

    public DashboardSummaryResponse summary() {
        Map<RouteStatus, Long> routesByStatus = new EnumMap<>(RouteStatus.class);
        for (RouteStatus status : RouteStatus.values()) {
            routesByStatus.put(status, routeRepository.countByStatus(status));
        }

        Map<DeliveryStatus, Long> ordersByStatus = new EnumMap<>(DeliveryStatus.class);
        for (DeliveryStatus status : DeliveryStatus.values()) {
            ordersByStatus.put(status, orderRepository.countByStatus(status));
        }

        long deliveredToday = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == DeliveryStatus.DELIVERED)
                .filter(order -> order.getUpdatedAt() != null && order.getUpdatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        long pendingPods = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == DeliveryStatus.DELIVERED)
                .filter(order -> !podRepository.existsByDeliveryId(order.getOrderId()))
                .count();

        return new DashboardSummaryResponse(
                userRepository.count(),
                driverRepository.count(),
                driverRepository.countByActiveTrue(),
                routeRepository.count(),
                routesByStatus,
                ordersByStatus,
                deliveredToday,
                pendingPods
        );
    }
}
