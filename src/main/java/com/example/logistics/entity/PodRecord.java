package com.example.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "pods", indexes = {
        @Index(name = "idx_pods_delivery_id", columnList = "delivery_id")
})
public class PodRecord {

    @jakarta.persistence.Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "delivery_id", nullable = false, length = 50)
    private String deliveryId;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Lob
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "customer_signature", length = 1000)
    private String customerSignature;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
}
