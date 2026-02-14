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


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UnifiedLoginDto request) {
        Object response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody OTPVerificationDto request) {
        var response = authService.verifyOTP(request);
        return ResponseEntity.ok(response);
    }
}