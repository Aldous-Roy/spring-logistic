package com.example.logistics.repository;

import com.example.logistics.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, UUID> {
    Optional<DriverLocation> findByDriverId(UUID driverId);
}
