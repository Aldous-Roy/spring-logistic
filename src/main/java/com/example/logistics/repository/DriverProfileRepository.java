package com.example.logistics.repository;

import com.example.logistics.entity.DriverProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {
    Optional<DriverProfile> findByEmployeeId(String employeeId);

    List<DriverProfile> findByActiveTrueOrderByCreatedAtDesc();

    long countByActiveTrue();

    boolean existsByEmployeeId(String employeeId);

    @Query("""
            select d
            from DriverProfile d
            where (:dispatcherId is null or d.dispatcherId = :dispatcherId)
              and (
               :search is null
               or :search = ''
               or lower(d.employeeId) like lower(concat('%', :search, '%'))
               or lower(d.firstName) like lower(concat('%', :search, '%'))
               or lower(d.lastName) like lower(concat('%', :search, '%'))
               or lower(d.phoneNumber) like lower(concat('%', :search, '%'))
              )
            """)
    Page<DriverProfile> search(@Param("search") String search, @Param("dispatcherId") String dispatcherId, Pageable pageable);
}
