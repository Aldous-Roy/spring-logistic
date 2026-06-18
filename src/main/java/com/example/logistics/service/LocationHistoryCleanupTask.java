package com.example.logistics.service;

import com.example.logistics.repository.DriverLocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationHistoryCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(LocationHistoryCleanupTask.class);
    private final DriverLocationHistoryRepository historyRepository;

    // Run every day at 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldHistory() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deletedCount = historyRepository.deleteOlderThan(cutoff);
        log.info("Cleaned up {} old location history records older than {}", deletedCount, cutoff);
    }
}
