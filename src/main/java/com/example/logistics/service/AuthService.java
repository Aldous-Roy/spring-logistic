package com.example.logistics.service;

import com.example.logistics.dto.auth.AuthResponse;
import com.example.logistics.dto.auth.ChangePasswordRequest;
import com.example.logistics.dto.auth.LoginRequest;
import com.example.logistics.dto.auth.SignupRequest;
import com.example.logistics.dto.auth.UserResponse;
import com.example.logistics.entity.AppUser;
import com.example.logistics.entity.enums.UserRole;
import com.example.logistics.exception.InvalidCredentialsException;
import com.example.logistics.repository.AppUserRepository;
import com.example.logistics.security.CustomUserDetails;
import com.example.logistics.security.JwtService;
import com.example.logistics.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final EmployeeIdGenerator employeeIdGenerator;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        AppUser user = new AppUser();
        String employeeId = employeeIdGenerator.generate();
        user.setEmployeeId(employeeId);
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);
        AppUser saved = userRepository.save(user);
        String token = jwtService.generateToken(new CustomUserDetails(saved));
        return new AuthResponse(token, "Bearer", jwtProperties.expirationMs(), saved.getEmployeeId(), saved.getName(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.employeeId(), request.password())
            );
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Invalid employee ID or password");
        }
        AppUser user = userRepository.findByEmployeeId(request.employeeId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid employee ID or password"));
        String token = jwtService.generateToken(new CustomUserDetails(user));
        return new AuthResponse(token, "Bearer", jwtProperties.expirationMs(), user.getEmployeeId(), user.getName(), user.getRole());
    }

    @Transactional
    public UserResponse changePassword(AppUser currentUser, ChangePasswordRequest request, PasswordEncoder encoder) {
        if (!encoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        currentUser.setPassword(encoder.encode(request.newPassword()));
        AppUser saved = userRepository.save(currentUser);
        return new UserResponse(saved.getId(), saved.getEmployeeId(), saved.getName(), saved.getRole(), saved.isActive());
    }
}
