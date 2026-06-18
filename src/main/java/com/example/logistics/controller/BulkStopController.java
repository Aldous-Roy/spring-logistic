package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.stop.CreateStopRequest;
import com.example.logistics.dto.stop.StopResponse;
import com.example.logistics.service.DeliveryOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stops-bulk")
@RequiredArgsConstructor
public class BulkStopController {

    private final DeliveryOrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<java.util.List<StopResponse>>> createBulk(@Valid @RequestBody java.util.List<CreateStopRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createBulk(requests), 200));
    }
}
