package com.example.logistics.service;

import com.example.logistics.dto.tracking.DriverLocationResponse;
import com.example.logistics.dto.tracking.DriverLocationUpsertRequest;
import com.example.logistics.entity.DriverLocation;
import com.example.logistics.exception.ResourceNotFoundException;
import com.example.logistics.repository.DriverLocationRepository;
import com.example.logistics.security.CurrentUserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final DriverLocationRepository locationRepository;
    private final DriverService driverService;
    private final CurrentUserFacade currentUserFacade;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public DriverLocationResponse upsert(DriverLocationUpsertRequest request) {
        UUID driverId = driverService.currentDriver().getDriverId();
        DriverLocation location = locationRepository.findByDriverId(driverId).orElseGet(DriverLocation::new);
        location.setDriverId(driverId);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setTimestamp(LocalDateTime.now());
        DriverLocation saved = locationRepository.save(location);
        DriverLocationResponse response = toResponse(saved);
        messagingTemplate.convertAndSend("/topic/locations/" + driverId, response);
        return response;
    }

    public DriverLocationResponse latest(UUID driverId) {
        return locationRepository.findByDriverId(driverId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Latest location not found for driver: " + driverId));
    }

    private DriverLocationResponse toResponse(DriverLocation location) {
        return new DriverLocationResponse(
                location.getDriverId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getTimestamp()
        );
    }
}
