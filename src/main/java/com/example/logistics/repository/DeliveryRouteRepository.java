package com.example.logistics.repository;

import com.example.logistics.entity.DeliveryRoute;
import com.example.logistics.entity.enums.RouteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, UUID> {
    Optional<DeliveryRoute> findByRouteCode(String routeCode);

    Page<DeliveryRoute> findAllByOrderByRouteDateDescCreatedAtDesc(Pageable pageable);

    Page<DeliveryRoute> findByRouteCodeContainingIgnoreCase(String routeCode, Pageable pageable);

    List<DeliveryRoute> findByDriverIdOrderByRouteDateAscCreatedAtAsc(UUID driverId);

    List<DeliveryRoute> findByDriverIdAndRouteDate(UUID driverId, LocalDate routeDate);

    long countByStatus(RouteStatus status);
}
