package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.common.PageResponse;
import com.example.logistics.dto.stop.FailDeliveryRequest;
import com.example.logistics.dto.stop.CreateStopRequest;
import com.example.logistics.dto.stop.StopResponse;
import com.example.logistics.dto.stop.UpdateStopStatusRequest;
import com.example.logistics.service.DeliveryOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class StopController {

    private final DeliveryOrderService orderService;

    /**
     * API: POST /api/stops
     * Method: create
     * Postman Request:
     * {
     *   "orderId": "ORD-1001",
     *   "routeCode": "RT-20260610-01",
     *   "customerName": "John Smith",
     *   "customerPhone": "9999999999",
     *   "deliveryAddress": "12 Main Street",
     *   "latitude": 12.9715987,
     *   "longitude": 77.594566,
     *   "timeWindowStart": "2026-06-10T10:00:00",
     *   "timeWindowEnd": "2026-06-10T12:00:00",
     *   "packageWeightKg": 1.25,
     *   "packageVolumeCbms": 0.010,
     *   "serviceTimeMins": 3,
     *   "requiredPodType": "PHOTO_REQUIRED"
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "orderId": "ORD-1001",
     *     "routeCode": "RT-20260610-01",
     *     "status": "PENDING"
     *   }
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<StopResponse>> create(@Valid @RequestBody CreateStopRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.create(request), 200));
    }

    /**
     * API: GET /api/stops
     * Method: list
     * Postman Request:
     * GET /api/stops?search=John&page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<PageResponse<StopResponse>>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.list(pageable, search), 200));
    }

    /**
     * API: PATCH /api/stops/{orderId}/status
     * Method: updateStatus
     * Postman Request:
     * {
     *   "status": "DELIVERED",
     *   "failedReasonNotes": null
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "orderId": "ORD-1001",
     *     "status": "DELIVERED"
     *   }
     * }
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('DRIVER','DISPATCHER')")
    public ResponseEntity<ApiResponse<StopResponse>> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateStopStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(orderId, request), 200));
    }

    /**
     * API: POST /api/stops/{orderId}/start-delivery
     * Method: start
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "orderId": "ORD-1001",
     *     "status": "OUT_FOR_DELIVERY"
     *   }
     * }
     */
    @PostMapping("/{orderId}/start-delivery")
    @PreAuthorize("hasAnyRole('DRIVER','DISPATCHER')")
    public ResponseEntity<ApiResponse<StopResponse>> start(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.startDelivery(orderId), 200));
    }

    /**
     * API: POST /api/stops/{orderId}/complete-delivery
     * Method: complete
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "orderId": "ORD-1001",
     *     "status": "DELIVERED"
     *   }
     * }
     */
    @PostMapping("/{orderId}/complete-delivery")
    @PreAuthorize("hasAnyRole('DRIVER','DISPATCHER')")
    public ResponseEntity<ApiResponse<StopResponse>> complete(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.completeDelivery(orderId), 200));
    }

    /**
     * API: POST /api/stops/{orderId}/fail-delivery
     * Method: fail
     * Postman Request:
     * {
     *   "reason": "Customer not available"
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "orderId": "ORD-1001",
     *     "status": "FAILED",
     *     "failedReasonNotes": "Customer not available"
     *   }
     * }
     */
    @PostMapping("/{orderId}/fail-delivery")
    @PreAuthorize("hasAnyRole('DRIVER','DISPATCHER')")
    public ResponseEntity<ApiResponse<StopResponse>> fail(
            @PathVariable String orderId,
            @Valid @RequestBody FailDeliveryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.failDelivery(orderId, request.reason()), 200));
    }

    @PatchMapping("/{orderId}/reassign")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<StopResponse>> reassign(
            @PathVariable String orderId,
            @Valid @RequestBody com.example.logistics.dto.stop.ReassignStopRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.reassign(orderId, request), 200));
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<PageResponse<StopResponse>>> getFailedStops(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getFailedStops(pageable), 200));
    }

    @PostMapping("/{orderId}/reschedule")
    public ResponseEntity<ApiResponse<StopResponse>> reschedule(
            @PathVariable String orderId,
            @Valid @RequestBody com.example.logistics.dto.stop.RescheduleStopRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.reschedule(orderId, request), 200));
    }
}
