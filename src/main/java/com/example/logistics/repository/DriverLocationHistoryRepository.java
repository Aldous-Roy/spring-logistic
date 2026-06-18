package com.example.logistics.repository;

import com.example.logistics.entity.DriverLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DriverLocationHistoryRepository extends JpaRepository<DriverLocationHistory, UUID> {
    
    List<DriverLocationHistory> findByDriverIdAndTimestampBetweenOrderByTimestampDesc(UUID driverId, LocalDateTime from, LocalDateTime to);

    @Modifying
    @Query("DELETE FROM DriverLocationHistory h WHERE h.timestamp < :cutoffDate")
    int deleteOlderThan(LocalDateTime cutoffDate);
}
