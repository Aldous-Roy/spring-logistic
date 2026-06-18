package com.example.logistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties properties) {
        String regionStr = properties.region();
        if (regionStr == null || regionStr.isBlank()) {
            regionStr = "us-east-1"; // Fallback default
        }
        return S3Client.builder()
                .region(Region.of(regionStr))
                .build();
    }
}
