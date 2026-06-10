package com.example.logistics.service;

import com.example.logistics.dto.pod.PodUploadResponse;
import com.example.logistics.dto.pod.PodUploadRequest;
import com.example.logistics.entity.PodRecord;
import com.example.logistics.exception.ResourceNotFoundException;
import com.example.logistics.repository.PodRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PodService {

    private final PodRecordRepository podRepository;
    private final DeliveryOrderService deliveryOrderService;

    @Transactional
    public PodUploadResponse upload(PodUploadRequest request) {
        deliveryOrderService.findOrder(request.deliveryId());

        PodRecord pod = new PodRecord();
        pod.setDeliveryId(request.deliveryId());
        pod.setImageData(readImageBytes(request.image()));
        pod.setContentType(resolveContentType(request.image()));
        pod.setOriginalFilename(request.image().getOriginalFilename());
        pod.setCustomerSignature(request.customerSignature());
        pod.setUploadedAt(LocalDateTime.now());
        PodRecord saved = podRepository.save(pod);
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/pods/")
                .path(saved.getId().toString())
                .toUriString();
        saved.setImageUrl(imageUrl);
        podRepository.save(saved);
        return new PodUploadResponse(saved.getId(), saved.getDeliveryId(), saved.getImageUrl(), saved.getCustomerSignature(), saved.getUploadedAt());
    }

    private byte[] readImageBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read POD image", ex);
        }
    }

    private String resolveContentType(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }
}
