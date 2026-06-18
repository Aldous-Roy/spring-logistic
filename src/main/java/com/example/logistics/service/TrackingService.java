package com.example.logistics.service;

import com.example.logistics.dto.tracking.DriverLocationResponse;
import com.example.logistics.dto.tracking.DriverLocationUpsertRequest;
import com.example.logistics.entity.DriverLocation;
import com.example.logistics.entity.DriverLocationHistory;
import com.example.logistics.exception.ResourceNotFoundException;
import com.example.logistics.repository.DriverLocationHistoryRepository;
import com.example.logistics.repository.DriverLocationRepository;
import com.example.logistics.security.CurrentUserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final DriverLocationRepository locationRepository;
    private final DriverLocationHistoryRepository historyRepository;
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

        DriverLocationHistory history = new DriverLocationHistory();
        history.setDriverId(driverId);
        history.setLatitude(request.latitude());
        history.setLongitude(request.longitude());
        history.setTimestamp(location.getTimestamp());
        historyRepository.save(history);

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

    @Transactional
    public List<DriverLocationResponse> bulkUpsert(List<DriverLocationUpsertRequest> requests) {
        UUID driverId = driverService.currentDriver().getDriverId();
        DriverLocationResponse lastResponse = null;

        for (DriverLocationUpsertRequest request : requests) {
            DriverLocation location = locationRepository.findByDriverId(driverId).orElseGet(DriverLocation::new);
            location.setDriverId(driverId);
            location.setLatitude(request.latitude());
            location.setLongitude(request.longitude());
            location.setTimestamp(LocalDateTime.now());
            DriverLocation saved = locationRepository.save(location);
            
            DriverLocationHistory history = new DriverLocationHistory();
            history.setDriverId(driverId);
            history.setLatitude(request.latitude());
            history.setLongitude(request.longitude());
            history.setTimestamp(location.getTimestamp());
            historyRepository.save(history);

            lastResponse = toResponse(saved);
        }

        // Broadcast only the final (most recent) location
        if (lastResponse != null) {
            messagingTemplate.convertAndSend("/topic/locations/" + driverId, lastResponse);
        }

        return requests.stream().map(req -> {
            DriverLocation loc = new DriverLocation();
            loc.setDriverId(driverId);
            loc.setLatitude(req.latitude());
            loc.setLongitude(req.longitude());
            loc.setTimestamp(LocalDateTime.now());
            return toResponse(loc);
        }).toList();
    }
}
