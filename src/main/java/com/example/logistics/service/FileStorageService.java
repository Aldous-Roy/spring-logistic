package com.example.logistics.service;

import com.example.logistics.config.StorageProperties;
import com.example.logistics.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;

    public String storePodImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("POD image is required");
        }
        try {
            Path directory = Paths.get(storageProperties.podDirectory()).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String originalName = file.getOriginalFilename() == null ? "pod" : file.getOriginalFilename();
            String extension = getExtension(originalName);
            String filename = UUID.randomUUID() + extension;
            Path target = directory.resolve(filename);
            Files.copy(file.getInputStream(), target);
            return filename;
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store POD image", ex);
        }
    }

    public Path resolvePodPath(String filename) {
        Path directory = Paths.get(storageProperties.podDirectory()).toAbsolutePath().normalize();
        return directory.resolve(filename).normalize();
    }

    private String getExtension(String originalName) {
        int index = originalName.lastIndexOf('.');
        return index >= 0 ? originalName.substring(index) : "";
    }
}
