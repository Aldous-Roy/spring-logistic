package com.example.logistics.service;

import com.example.logistics.dto.pod.PodUploadResponse;
import com.example.logistics.dto.pod.PodUploadRequest;
import com.example.logistics.entity.PodRecord;
import com.example.logistics.repository.PodRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PodService {

    private final PodRecordRepository podRepository;
    private final DeliveryOrderService deliveryOrderService;
    private final FileStorageService fileStorageService;

    @Transactional
    public PodUploadResponse upload(PodUploadRequest request) {
        deliveryOrderService.findOrder(request.deliveryId());

        String s3Url = fileStorageService.storePodImage(request.image());

        PodRecord pod = new PodRecord();
        pod.setDeliveryId(request.deliveryId());
        // Since we're using S3, we might not need to save the bytes to the DB.
        // However, to avoid breaking existing DB schemas right away, we can still save it
        // or just leave imageData null if we modify the schema. For now, we'll keep saving
        // the bytes in the DB for backward compatibility, but in a real migration we'd drop it.
        pod.setImageData(readImageBytes(request.image()));
        pod.setContentType(resolveContentType(request.image()));
        pod.setOriginalFilename(request.image().getOriginalFilename());
        pod.setCustomerSignature(request.customerSignature());
        pod.setUploadedAt(LocalDateTime.now());
        pod.setImageUrl(s3Url);
        pod.setLatitude(request.latitude());
        pod.setLongitude(request.longitude());
        if (request.capturedAt() != null && !request.capturedAt().isBlank()) {
            try {
                pod.setCapturedAt(LocalDateTime.parse(request.capturedAt()));
            } catch (Exception e) {
                // Ignore parse errors, fallback to uploadedAt if needed
            }
        }
        PodRecord saved = podRepository.save(pod);
        
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
