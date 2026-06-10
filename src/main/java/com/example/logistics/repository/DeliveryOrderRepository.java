package com.example.logistics.repository;

import com.example.logistics.entity.DeliveryOrder;
import com.example.logistics.entity.DeliveryRoute;
import com.example.logistics.entity.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, String> {
    Page<DeliveryOrder> findByRoute(DeliveryRoute route, Pageable pageable);

    List<DeliveryOrder> findByRouteOrderBySequenceNumberAscCreatedAtAsc(DeliveryRoute route);

    long countByStatus(DeliveryStatus status);

    long countByRoute_RouteDate(LocalDate routeDate);
}
