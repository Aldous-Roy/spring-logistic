package com.example.logistics.repository;

import com.example.logistics.entity.DriverAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverAttendanceRepository extends JpaRepository<DriverAttendance, UUID> {
    Optional<DriverAttendance> findByDriverId(UUID driverId);
}
