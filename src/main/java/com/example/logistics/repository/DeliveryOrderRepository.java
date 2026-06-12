package com.example.logistics.repository;

import com.example.logistics.entity.DeliveryOrder;
import com.example.logistics.entity.DeliveryRoute;
import com.example.logistics.entity.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, String> {
    Page<DeliveryOrder> findByRoute(DeliveryRoute route, Pageable pageable);

    List<DeliveryOrder> findByRouteOrderBySequenceNumberAscCreatedAtAsc(DeliveryRoute route);

    long countByStatus(DeliveryStatus status);

    long countByRoute_RouteDate(LocalDate routeDate);

    @Query("""
            select o
            from DeliveryOrder o
            left join o.route r
            where :search is null
               or :search = ''
               or lower(o.orderId) like lower(concat('%', :search, '%'))
               or lower(o.customerName) like lower(concat('%', :search, '%'))
               or lower(o.customerPhone) like lower(concat('%', :search, '%'))
               or lower(o.deliveryAddress) like lower(concat('%', :search, '%'))
               or lower(r.routeCode) like lower(concat('%', :search, '%'))
            """)
    Page<DeliveryOrder> search(@Param("search") String search, Pageable pageable);
}
