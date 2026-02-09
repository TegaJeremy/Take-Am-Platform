package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.*;
import com.takeam.userservice.dto.response.*;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.model.User;
import com.takeam.userservice.service.TraderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/traders")
@RequiredArgsConstructor
@Slf4j
public class TraderController {

    private final TraderService traderService;
    private final UserMapper userMapper;

    //public endpoints

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerTrader(
            @Valid @RequestBody TraderRegistrationRequestDto request) {

        AuthResponseDto response = traderService.registerTrader(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<TokenResponseDto> verifyOTP(
            @Valid @RequestBody OTPVerificationDto request) {

        TokenResponseDto response = traderService.verifyOTP(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponseDto> resendOTP(
            @Valid @RequestBody OTPRequestDto request) {

        AuthResponseDto response = traderService.resendOTP(request.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    // protected endpoints

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile(
            @AuthenticationPrincipal User user) {

        UserResponseDto response = userMapper.toUserResponseDto(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    public ResponseEntity<TraderDetailResponseDto> getTraderDetails(
            @AuthenticationPrincipal User user) {

        TraderDetailResponseDto response = traderService.getTraderDetails(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('TRADER', 'AGENT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TraderDetailResponseDto> getTraderByUserId(@PathVariable UUID userId) {
        log.info("Fetching trader details for user ID: {}", userId);
        TraderDetailResponseDto response = traderService.getTraderDetailsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<TraderDetailResponseDto> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateTraderProfileDto request) {

        TraderDetailResponseDto response = traderService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-phone")
    public ResponseEntity<UserResponseDto> changePhoneNumber(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePhoneNumberDto request) {

        UserResponseDto response = traderService.changePhoneNumber(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<Void> deactivateAccount(@AuthenticationPrincipal User user) {
        traderService.deactivateAccount(user.getId());
        return ResponseEntity.noContent().build();
    }
}