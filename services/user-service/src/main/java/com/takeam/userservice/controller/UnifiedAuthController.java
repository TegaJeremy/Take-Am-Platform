package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.UnifiedLoginDto;
import com.takeam.userservice.dto.request.OTPVerificationDto;
import com.takeam.userservice.service.UnifiedAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class UnifiedAuthController {

    private final UnifiedAuthService authService;

    /**
     * UNIFIED LOGIN - Handles all user types
     *
     * Traders: Send phone → Get OTP
     * Others: Send email + password → Get JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UnifiedLoginDto request) {
        Object response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP (for traders only)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody OTPVerificationDto request) {
        var response = authService.verifyOTP(request);
        return ResponseEntity.ok(response);
    }
}