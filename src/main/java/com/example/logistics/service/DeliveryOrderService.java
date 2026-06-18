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
import com.example.logistics.repository.PodRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryOrderService {

    private final DeliveryOrderRepository orderRepository;
    private final RouteService routeService;
    private final SmsNotificationService smsService;
    private final PodRecordRepository podRepository;

    @Transactional
    public StopResponse create(CreateStopRequest request) {
        BigDecimal weight = request.packageWeightKg() == null ? new BigDecimal("1.00") : request.packageWeightKg();
        if (weight.compareTo(new BigDecimal("300.00")) > 0) {
            throw new InvalidOperationException("Package weight cannot exceed 300kg. Order is unroutable.");
        }

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
        order.setDeliveryDate(request.deliveryDate() != null ? request.deliveryDate() : java.time.LocalDate.now());
        order.setPackageWeightKg(weight);
        order.setPackageVolumeCbms(request.packageVolumeCbms() == null ? new BigDecimal("0.010") : request.packageVolumeCbms());
        order.setServiceTimeMins(request.serviceTimeMins() == null ? 3 : request.serviceTimeMins());
        order.setRequiredPodType(request.requiredPodType() == null ? PodRequirement.PHOTO_REQUIRED : request.requiredPodType());
        order.setStatus(DeliveryStatus.PENDING);
        return toResponse(orderRepository.save(order));
    }

    public PageResponse<StopResponse> listByRoute(UUID routeId, Pageable pageable) {
        DeliveryRoute route = routeService.findRoute(routeId);
        Pageable mappedPageable = mapStopSort(pageable);
        Page<StopResponse> page = orderRepository.findByRoute(route, mappedPageable).map(this::toResponse);
        return PageResponse.from(page);
    }

    public PageResponse<StopResponse> list(Pageable pageable, String search) {
        Pageable mappedPageable = mapStopSort(pageable);
        Page<DeliveryOrder> page = orderRepository.search(StringUtils.hasText(search) ? search : null, mappedPageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public StopResponse updateStatus(String orderId, UpdateStopStatusRequest request) {
        DeliveryOrder order = findOrder(orderId);
        order.setStatus(request.status());
        if (request.status() == DeliveryStatus.FAILED || request.status() == DeliveryStatus.ATTEMPTED_ABSENT || request.status() == DeliveryStatus.ATTEMPTED_NO_ACCESS) {
            if (request.failedReasonNotes() != null) {
                order.setFailedReasonNotes(request.failedReasonNotes());
            } else {
                order.setFailedReasonNotes(request.status().name());
            }
            order.setStatus(DeliveryStatus.FAILED);
            String reason = order.getFailedReasonNotes();
            String link = "https://logistic-captain.com/reschedule?orderId=" + order.getOrderId();
            smsService.sendDeliveryFailedAlert(order, reason, link, "+1234567890");
        }
        if (request.status() == DeliveryStatus.DELIVERED) {
            order.setStatus(DeliveryStatus.DELIVERED);
            order.setFailedReasonNotes(null);
            sendCompletionSms(order);
        }
        if (request.status() == DeliveryStatus.OUT_FOR_DELIVERY) {
            smsService.sendDriverNearbyAlert(order, order.getEstimatedArrivalTime() != null ? order.getEstimatedArrivalTime().toString() : "soon");
        }
        DeliveryOrder saved = orderRepository.save(order);
        if (saved.getStatus() == DeliveryStatus.DELIVERED || saved.getStatus() == DeliveryStatus.FAILED) {
            triggerTwoStopsAwaySmsIfApplicable(saved);
        }
        return toResponse(saved);
    }

    private void sendCompletionSms(DeliveryOrder order) {
        String podUrl = podRepository.findByDeliveryIdOrderByUploadedAtDesc(order.getOrderId())
                .stream().findFirst().map(p -> p.getImageUrl()).orElse("No signature provided");
        smsService.sendDeliveryCompletedAlert(order, podUrl);
    }

    @Transactional
    public StopResponse startDelivery(String orderId) {
        DeliveryOrder order = findOrder(orderId);
        if (order.getStatus() != DeliveryStatus.ROUTED && order.getStatus() != DeliveryStatus.PENDING) {
            throw new InvalidOperationException("Delivery can only be started from PENDING or ROUTED status");
        }
        order.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        smsService.sendDriverNearbyAlert(order, order.getEstimatedArrivalTime() != null ? order.getEstimatedArrivalTime().toString() : "soon");
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public StopResponse completeDelivery(String orderId) {
        DeliveryOrder order = findOrder(orderId);
        order.setStatus(DeliveryStatus.DELIVERED);
        order.setFailedReasonNotes(null);
        sendCompletionSms(order);
        DeliveryOrder saved = orderRepository.save(order);
        triggerTwoStopsAwaySmsIfApplicable(saved);
        return toResponse(saved);
    }

    @Transactional
    public StopResponse failDelivery(String orderId, String reason) {
        DeliveryOrder order = findOrder(orderId);
        order.setStatus(DeliveryStatus.FAILED);
        order.setFailedReasonNotes(reason);
        String link = "https://logistic-captain.com/reschedule?orderId=" + order.getOrderId();
        smsService.sendDeliveryFailedAlert(order, reason, link, "+1234567890");
        DeliveryOrder saved = orderRepository.save(order);
        triggerTwoStopsAwaySmsIfApplicable(saved);
        return toResponse(saved);
    }

    private void triggerTwoStopsAwaySmsIfApplicable(DeliveryOrder completedOrFailedOrder) {
        DeliveryRoute route = completedOrFailedOrder.getRoute();
        if (route == null || completedOrFailedOrder.getSequenceNumber() == null) {
            return;
        }
        int nextSecondSequence = completedOrFailedOrder.getSequenceNumber() + 2;
        orderRepository.findByRouteAndSequenceNumber(route, nextSecondSequence).ifPresent(nextSecondOrder -> {
            if (nextSecondOrder.getStatus() == DeliveryStatus.PENDING || nextSecondOrder.getStatus() == DeliveryStatus.ROUTED) {
                String eta = nextSecondOrder.getEstimatedArrivalTime() != null 
                        ? nextSecondOrder.getEstimatedArrivalTime().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")) 
                        : "soon";
                smsService.sendDriverNearbyAlert(nextSecondOrder, eta);
            }
        });
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
                order.getDeliveryDate(),
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

    private Pageable mapStopSort(Pageable pageable) {
        Sort mappedSort = Sort.by(pageable.getSort().stream()
                .map(order -> "routeCode".equals(order.getProperty())
                        ? new Sort.Order(order.getDirection(), "route.routeCode")
                        : order)
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mappedSort);
    }
}
