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

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, String>, JpaSpecificationExecutor<DeliveryOrder> {
    Page<DeliveryOrder> findByRoute(DeliveryRoute route, Pageable pageable);

    List<DeliveryOrder> findByRouteOrderBySequenceNumberAscCreatedAtAsc(DeliveryRoute route);

    java.util.Optional<DeliveryOrder> findByRouteAndSequenceNumber(DeliveryRoute route, Integer sequenceNumber);

    long countByStatus(DeliveryStatus status);

    long countByRoute_RouteDate(LocalDate routeDate);

    List<DeliveryOrder> findByDeliveryDateAndStatus(LocalDate deliveryDate, DeliveryStatus status);

    @Query("""
            select o
            from DeliveryOrder o
            left join o.route r
            where (:dispatcherId is null or o.dispatcherId = :dispatcherId)
              and (
               :search is null
               or :search = ''
               or lower(o.orderId) like lower(concat('%', :search, '%'))
               or lower(o.customerName) like lower(concat('%', :search, '%'))
               or lower(o.customerPhone) like lower(concat('%', :search, '%'))
               or lower(o.deliveryAddress) like lower(concat('%', :search, '%'))
               or lower(r.routeCode) like lower(concat('%', :search, '%'))
              )
            """)
    Page<DeliveryOrder> search(@Param("search") String search, @Param("dispatcherId") String dispatcherId, Pageable pageable);
}
