package com.takeam.userservice.controller;

import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.request.OTPRequestDto;
import com.takeam.userservice.dto.request.OTPVerificationDto;
import com.takeam.userservice.dto.response.TokenResponseDto;
import com.takeam.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login/request-otp")
    public ResponseEntity<AuthResponseDto> requestLoginOTP(@Valid @RequestBody OTPRequestDto request) {
        log.info("Login OTP request received for: {}", request.getPhoneNumber());
        AuthResponseDto response = authService.requestLoginOTP(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login/verify-otp")
    public ResponseEntity<TokenResponseDto> verifyLoginOTP(@Valid @RequestBody OTPVerificationDto request) {
        log.info("OTP verification request for: {}", request.getPhoneNumber());
        TokenResponseDto response = authService.verifyLoginOTP(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Resend login OTP
     * POST /api/v1/auth/login/resend-otp
     */
    @PostMapping("/login/resend-otp")
    public ResponseEntity<AuthResponseDto> resendLoginOTP(@Valid @RequestBody OTPRequestDto request) {
        log.info("Resend OTP request for: {}", request.getPhoneNumber());
        AuthResponseDto response = authService.resendLoginOTP(request);
        return ResponseEntity.ok(response);
    }
}