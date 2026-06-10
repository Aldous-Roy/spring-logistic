package com.example.logistics.repository;

import com.example.logistics.entity.PodRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PodRecordRepository extends JpaRepository<PodRecord, UUID> {
    List<PodRecord> findByDeliveryIdOrderByUploadedAtDesc(String deliveryId);

    boolean existsByDeliveryId(String deliveryId);
}
