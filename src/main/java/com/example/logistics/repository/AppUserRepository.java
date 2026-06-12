package com.example.logistics.repository;

import com.example.logistics.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    Page<AppUser> findByEmployeeIdContainingIgnoreCaseOrNameContainingIgnoreCase(
            String employeeId,
            String name,
            Pageable pageable
    );
}
