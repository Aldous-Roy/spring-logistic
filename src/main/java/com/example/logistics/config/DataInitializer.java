package com.example.logistics.config;

import com.example.logistics.entity.AppUser;
import com.example.logistics.entity.DriverProfile;
import com.example.logistics.entity.enums.UserRole;
import com.example.logistics.repository.AppUserRepository;
import com.example.logistics.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if database is empty
        if (userRepository.count() == 0) {
            log.info("Initializing test data...");
            
            // Test user 1: Dispatcher
            AppUser dispatcher = new AppUser();
            dispatcher.setEmployeeId("EMP1001");
            dispatcher.setName("Alice Dispatcher");
            dispatcher.setPassword(passwordEncoder.encode("TestPassword123"));
            dispatcher.setRole(UserRole.DISPATCHER);
            dispatcher.setActive(true);
            userRepository.save(dispatcher);
            log.info("Created test user: EMP1001 (Dispatcher)");
            
            // Test user 2: Driver
            AppUser driver = new AppUser();
            driver.setEmployeeId("EMP1002");
            driver.setName("Bob Driver");
            driver.setPassword(passwordEncoder.encode("TestPassword123"));
            driver.setRole(UserRole.DRIVER);
            driver.setActive(true);
            userRepository.save(driver);
            log.info("Created test user: EMP1002 (Driver)");

            if (!driverProfileRepository.existsByEmployeeId("EMP1002")) {
                DriverProfile driverProfile = new DriverProfile();
                driverProfile.setEmployeeId("EMP1002");
                driverProfile.setFirstName("Bob");
                driverProfile.setLastName("Driver");
                driverProfile.setPhoneNumber("9999999999");
                driverProfile.setMaxPackageCapacity(50);
                driverProfile.setMaxWeightCapacityKg(new java.math.BigDecimal("300.00"));
                driverProfile.setActive(false);
                driverProfileRepository.save(driverProfile);
                log.info("Created matching driver profile for EMP1002");
            }
            
            // Test user 3: Admin
            AppUser admin = new AppUser();
            admin.setEmployeeId("EMP1003");
            admin.setName("Charlie Admin");
            admin.setPassword(passwordEncoder.encode("TestPassword123"));
            admin.setRole(UserRole.SUPER_ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Created test user: EMP1003 (Super Admin)");
            
            // Test user 4: Fleet Manager
            AppUser fleetManager = new AppUser();
            fleetManager.setEmployeeId("EMP1004");
            fleetManager.setName("Diana Fleet Manager");
            fleetManager.setPassword(passwordEncoder.encode("TestPassword123"));
            fleetManager.setRole(UserRole.FLEET_MANGER);
            fleetManager.setActive(true);
            userRepository.save(fleetManager);
            log.info("Created test user: EMP1004 (Fleet Manager)");
            
            log.info("✅ Test data initialized successfully!");
            log.info("📝 Login credentials: Use any of the above employeeIds with password: TestPassword123");
        }
    }
}
