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
        String filename = fileStorageService.storePodImage(request.image());
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/pods/")
                .path(filename)
                .toUriString();

        PodRecord pod = new PodRecord();
        pod.setDeliveryId(request.deliveryId());
        pod.setImageUrl(imageUrl);
        pod.setCustomerSignature(request.customerSignature());
        pod.setUploadedAt(LocalDateTime.now());
        PodRecord saved = podRepository.save(pod);
        return new PodUploadResponse(saved.getId(), saved.getDeliveryId(), saved.getImageUrl(), saved.getCustomerSignature(), saved.getUploadedAt());
    }
}
