package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.BuyerRegistrationDto;
import com.takeam.userservice.dto.request.EmailRequestDto;
import com.takeam.userservice.dto.request.OTPVerificationDto;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.response.TokenResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.service.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerBuyer(
            @RequestBody BuyerRegistrationDto dto) {
        return ResponseEntity.ok(buyerService.registerBuyer(dto));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<TokenResponseDto> verifyEmail(
            @RequestBody OTPVerificationDto dto) {
        return ResponseEntity.ok(buyerService.verifyEmailOTP(dto));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponseDto> resendOtp(
            @Valid @RequestBody EmailRequestDto dto) {

        AuthResponseDto response = buyerService.resendEmailOTP(dto.getEmail());

        return ResponseEntity.ok(response);
    }

}

