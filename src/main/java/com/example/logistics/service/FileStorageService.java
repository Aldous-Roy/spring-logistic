package com.example.logistics.service;

import com.example.logistics.config.S3Properties;
import com.example.logistics.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String storePodImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("POD image is required");
        }
        try {
            String originalName = file.getOriginalFilename() == null ? "pod" : file.getOriginalFilename();
            String extension = getExtension(originalName);
            String filename = "pods/" + UUID.randomUUID() + extension;
            
            String bucket = s3Properties.bucket();
            if (bucket == null || bucket.isBlank()) {
                throw new FileStorageException("AWS S3 Bucket name is not configured");
            }

            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Return the public URL
            String region = s3Properties.region() != null && !s3Properties.region().isBlank() ? s3Properties.region() : "us-east-1";
            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + filename;

        } catch (IOException ex) {
            throw new FileStorageException("Failed to read POD image for S3 upload", ex);
        } catch (Exception ex) {
            throw new FileStorageException("Failed to upload POD image to S3", ex);
        }
    }

    private String getExtension(String originalName) {
        int index = originalName.lastIndexOf('.');
        return index >= 0 ? originalName.substring(index) : "";
    }
}
