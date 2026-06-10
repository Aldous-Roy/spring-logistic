package com.example.logistics.controller;

import com.example.logistics.entity.PodRecord;
import com.example.logistics.repository.PodRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final PodRecordRepository podRecordRepository;

    /**
     * API: GET /api/files/pods/{filename}
     * Method: getPodFile
     * Postman Request:
     * GET /api/files/pods/pod.png
     * Postman Response:
     * 200 OK with image bytes or 404 if file is missing
     */
    @GetMapping("/pods/{podId}")
    public ResponseEntity<byte[]> getPodFile(@PathVariable UUID podId) {
        PodRecord pod = podRecordRepository.findById(podId)
                .orElse(null);
        if (pod == null) {
            return ResponseEntity.notFound().build();
        }
        String contentType = pod.getContentType();
        return ResponseEntity.ok()
                .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename(pod) + "\"")
                .body(pod.getImageData());
    }

    private String safeFilename(PodRecord pod) {
        return pod.getOriginalFilename() == null || pod.getOriginalFilename().isBlank()
                ? pod.getId().toString()
                : pod.getOriginalFilename();
    }
}
