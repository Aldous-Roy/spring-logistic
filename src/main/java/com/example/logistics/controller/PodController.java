package com.example.logistics.controller;

import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.dto.pod.PodUploadRequest;
import com.example.logistics.dto.pod.PodUploadResponse;
import com.example.logistics.service.PodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pods")
@RequiredArgsConstructor
public class PodController {

    private final PodService podService;

    /**
     * API: POST /api/pods/upload
     * Method: upload
     * Postman Request (form-data):
     * deliveryId=ORD-1001
     * image=<file>
     * customerSignature=John Smith
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "id": "4d6a49b7-8b75-4f2f-b6c5-9f5f9e4c6f61",
     *     "deliveryId": "ORD-1001",
     *     "imageUrl": "http://localhost:8080/api/files/pods/pod.png",
     *     "customerSignature": "John Smith"
     *   }
     * }
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DRIVER','DISPATCHER')")
    public ResponseEntity<ApiResponse<PodUploadResponse>> upload(@ModelAttribute PodUploadRequest request) {
        return ResponseEntity.ok(ApiResponse.success(podService.upload(request), 200));
    }
}
