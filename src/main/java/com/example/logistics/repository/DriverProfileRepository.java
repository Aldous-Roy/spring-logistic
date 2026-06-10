package com.example.logistics.repository;

import com.example.logistics.entity.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {
    Optional<DriverProfile> findByEmployeeId(String employeeId);

    List<DriverProfile> findByActiveTrueOrderByCreatedAtDesc();

    long countByActiveTrue();

    boolean existsByEmployeeId(String employeeId);
}
