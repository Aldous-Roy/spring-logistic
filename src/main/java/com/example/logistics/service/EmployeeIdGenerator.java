package com.example.logistics.service;

import com.example.logistics.repository.AppUserRepository;
import com.example.logistics.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final AppUserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;

    public String generate() {
        for (int attempts = 0; attempts < 20; attempts++) {
            String candidate = "EMP-" + LocalDate.now().toString().replace("-", "") + "-" + randomSuffix(6);
            if (!userRepository.existsByEmployeeId(candidate) && !driverProfileRepository.existsByEmployeeId(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique employee ID");
    }

    private String randomSuffix(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return builder.toString();
    }
}
