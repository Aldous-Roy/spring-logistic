package com.example.logistics.controller;

import com.example.logistics.dto.auth.AuthResponse;
import com.example.logistics.dto.auth.LoginRequest;
import com.example.logistics.dto.auth.SignupRequest;
import com.example.logistics.dto.common.ApiResponse;
import com.example.logistics.service.AuthService;
import com.example.logistics.security.CurrentUserFacade;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserFacade currentUserFacade;

    /**
     * API: POST /api/auth/signup
     * Method: signup
     * Postman Request:
     * {
     *   "name": "Alice Dispatcher",
     *   "password": "Password123!",
     *   "role": "DISPATCHER"
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "token": "jwt-token",
     *     "tokenType": "Bearer",
     *     "expiresInMs": 86400000,
     *     "employeeId": "EMP-20260610-AB12CD",
     *     "name": "Alice Dispatcher",
     *     "role": "DISPATCHER"
     *   }
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(request), 200));
    }

    /**
     * API: POST /api/auth/login
     * Method: login
     * Postman Request:
     * {
     *   "employeeId": "EMP1001",
     *   "password": "Password123!"
     * }
     * Postman Response:
     * {
     *   "status": "success",
     *   "statusCode": 200,
     *   "data": {
     *     "token": "jwt-token",
     *     "tokenType": "Bearer",
     *     "expiresInMs": 86400000,
     *     "employeeId": "EMP1001",
     *     "name": "Alice Dispatcher",
     *     "role": "DISPATCHER"
     *   }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request), 200));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> logout() {
        authService.logout(currentUserFacade.currentUser());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", 200));
    }
}
