package com.example.logistics.service;

import com.example.logistics.dto.common.PageResponse;
import com.example.logistics.dto.stop.CreateStopRequest;
import com.example.logistics.dto.stop.StopResponse;
import com.example.logistics.dto.stop.UpdateStopStatusRequest;
import com.example.logistics.entity.DeliveryOrder;
import com.example.logistics.entity.DeliveryRoute;
import com.example.logistics.entity.enums.DeliveryStatus;
import com.example.logistics.entity.enums.PodRequirement;
import com.example.logistics.exception.InvalidOperationException;
import com.example.logistics.exception.ResourceNotFoundException;
import com.example.logistics.repository.DeliveryOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryOrderService {

    private final DeliveryOrderRepository orderRepository;
    private final RouteService routeService;

    @Transactional
    public StopResponse create(CreateStopRequest request) {
        DeliveryOrder order = new DeliveryOrder();
        order.setOrderId(request.orderId());
        if (request.routeCode() != null && !request.routeCode().isBlank()) {
            DeliveryRoute route = routeService.findRouteByCode(request.routeCode());
            order.setRoute(route);
        }
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setLatitude(request.latitude());
        order.setLongitude(request.longitude());
        order.setTimeWindowStart(request.timeWindowStart());
        order.setTimeWindowEnd(request.timeWindowEnd());
        order.setPackageWeightKg(request.packageWeightKg() == null ? new BigDecimal("1.00") : request.packageWeightKg());
        order.setPackageVolumeCbms(request.packageVolumeCbms() == null ? new BigDecimal("0.010") : request.packageVolumeCbms());
        order.setServiceTimeMins(request.serviceTimeMins() == null ? 3 : request.serviceTimeMins());
        order.setRequiredPodType(request.requiredPodType() == null ? PodRequirement.PHOTO_REQUIRED : request.requiredPodType());
        order.setStatus(DeliveryStatus.PENDING);
        return toResponse(orderRepository.save(order));
    }

    public PageResponse<StopResponse> listByRoute(UUID routeId, Pageable pageable) {
        DeliveryRoute route = routeService.findRoute(routeId);
        Page<StopResponse> page = orderRepository.findByRoute(route, pageable).map(this::toResponse);
        return PageResponse.from(page);
    }

    @Transactional
    public StopResponse updateStatus(String orderId, UpdateStopStatusRequest request) {
        DeliveryOrder order = findOrder(orderId);
        order.setStatus(request.status());
        if (request.status() == DeliveryStatus.FAILED && request.failedReasonNotes() != null) {
            order.setFailedReasonNotes(request.failedReasonNotes());
        }
        if (request.status() == DeliveryStatus.DELIVERED) {
            order.setFailedReasonNotes(null);
        }
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public StopResponse startDelivery(String orderId) {
        DeliveryOrder order = findOrder(orderId);
        if (order.getStatus() != DeliveryStatus.ROUTED && order.getStatus() != DeliveryStatus.PENDING) {
            throw new InvalidOperationException("Delivery can only be started from PENDING or ROUTED status");
        }
        order.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public StopResponse completeDelivery(String orderId) {
        DeliveryOrder order = findOrder(orderId);
        order.setStatus(DeliveryStatus.DELIVERED);
        order.setFailedReasonNotes(null);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public StopResponse failDelivery(String orderId, String reason) {
        DeliveryOrder order = findOrder(orderId);
        order.setStatus(DeliveryStatus.FAILED);
        order.setFailedReasonNotes(reason);
        return toResponse(orderRepository.save(order));
    }

    public DeliveryOrder findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery order not found: " + orderId));
    }

    private StopResponse toResponse(DeliveryOrder order) {
        return new StopResponse(
                order.getOrderId(),
                order.getRoute() == null ? null : order.getRoute().getRouteCode(),
                order.getSequenceNumber(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryAddress(),
                order.getLatitude(),
                order.getLongitude(),
                order.getTimeWindowStart(),
                order.getTimeWindowEnd(),
                order.getPackageWeightKg(),
                order.getPackageVolumeCbms(),
                order.getServiceTimeMins(),
                order.getStatus(),
                order.getRequiredPodType(),
                order.getEstimatedArrivalTime(),
                order.getFailedReasonNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
