package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.common.PageResponse;
import com.example.logistics.dto.route.AssignDriverRequest;
import com.example.logistics.dto.route.AssignOrdersRequest;
import com.example.logistics.dto.route.RouteCreateRequest;
import com.example.logistics.dto.route.RouteResponse;
import com.example.logistics.dto.stop.StopResponse;
import com.example.logistics.service.DeliveryOrderService;
import com.example.logistics.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final DeliveryOrderService orderService;

    /**
     * API: POST /api/routes
     * Method: create
     * Postman Request:
     * {
     *   "routeCode": "RT-20260610-01",
     *   "routeDate": "2026-06-10",
     *   "totalDistanceKm": 42.5,
     *   "estimatedDurationMins": 120,
     *   "routePolyline": "encoded-polyline"
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "routeCode": "RT-20260610-01",
     *     "routeDate": "2026-06-10",
     *     "status": "DRAFT",
     *     "driverId": null,
     *     "totalDistanceKm": 42.5,
     *     "estimatedDurationMins": 120,
     *     "routePolyline": "encoded-polyline"
     *   }
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> create(@Valid @RequestBody RouteCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(routeService.create(request), 200));
    }

    /**
     * API: GET /api/routes
     * Method: list
     * Postman Request:
     * GET /api/routes?page=0&size=10&sort=routeDate,desc
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "content": [
     *       {
     *         "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *         "routeCode": "RT-20260610-01",
     *         "routeDate": "2026-06-10",
     *         "status": "DRAFT"
     *       }
     *     ],
     *     "pageNumber": 0,
     *     "pageSize": 10,
     *     "totalElements": 1,
     *     "totalPages": 1,
     *     "first": true,
     *     "last": true
     *   }
     * }
     */
    @GetMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<PageResponse<RouteResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(routeService.list(pageable), 200));
    }

    /**
     * API: GET /api/routes/{id}
     * Method: getById
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "routeCode": "RT-20260610-01",
     *     "routeDate": "2026-06-10",
     *     "status": "DRAFT"
     *   }
     * }
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.getById(id), 200));
    }

    /**
     * API: POST /api/routes/{id}/assign-driver
     * Method: assignDriver
     * Postman Request:
     * {
     *   "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21"
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "routeCode": "RT-20260610-01",
     *     "driverId": "0d8f9f2c-3d91-4c8d-9f55-1a7c8a7d2b21"
     *   }
     * }
     */
    @PostMapping("/{id}/assign-driver")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> assignDriver(@PathVariable UUID id, @Valid @RequestBody AssignDriverRequest request) {
        return ResponseEntity.ok(ApiResponse.success(routeService.assignDriver(id, request), 200));
    }

    /**
     * API: POST /api/routes/{id}/assign-orders
     * Method: assignOrders
     * Postman Request:
     * {
     *   "orderIds": ["ORD-1001", "ORD-1002"]
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "routeCode": "RT-20260610-01"
     *   }
     * }
     */
    @PostMapping("/{id}/assign-orders")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> assignOrders(@PathVariable UUID id, @Valid @RequestBody AssignOrdersRequest request) {
        return ResponseEntity.ok(ApiResponse.success(routeService.assignOrders(id, request), 200));
    }

    /**
     * API: POST /api/routes/{id}/publish
     * Method: publish
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "status": "PUBLISHED"
     *   }
     * }
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.publish(id), 200));
    }

    /**
     * API: POST /api/routes/{id}/activate
     * Method: activate
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "status": "ACTIVE"
     *   }
     * }
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.activate(id), 200));
    }

    /**
     * API: POST /api/routes/{id}/complete
     * Method: complete
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "status": "COMPLETED"
     *   }
     * }
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.complete(id), 200));
    }

    /**
     * API: POST /api/routes/{id}/cancel
     * Method: cancel
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "routeId": "c0a8013d-2b3e-41b1-a3a7-f4d4b1a1f111",
     *     "status": "CANCELLED"
     *   }
     * }
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<RouteResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.cancel(id), 200));
    }

    /**
     * API: GET /api/routes/{routeId}/stops
     * Method: stops
     * Postman Request:
     * GET /api/routes/{routeId}/stops?page=0&size=10
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "content": [
     *       {
     *         "orderId": "ORD-1001",
     *         "routeCode": "RT-20260610-01",
     *         "status": "ROUTED"
     *       }
     *     ],
     *     "pageNumber": 0,
     *     "pageSize": 10,
     *     "totalElements": 1,
     *     "totalPages": 1,
     *     "first": true,
     *     "last": true
     *   }
     * }
     */
    @GetMapping("/{routeId}/stops")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<PageResponse<StopResponse>>> stops(@PathVariable UUID routeId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.listByRoute(routeId, pageable), 200));
    }
}
