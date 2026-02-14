package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.ChangePasswordDto;
import com.takeam.userservice.dto.request.EmailRequestDto;
import com.takeam.userservice.dto.request.ResetPasswordDto;
import com.takeam.userservice.dto.response.MessageResponseDto;
import com.takeam.userservice.model.User;
import com.takeam.userservice.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {

    private final PasswordService passwordService;


    @PostMapping("/change")
    public ResponseEntity<MessageResponseDto> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordDto request) {

        log.info("Password change request from user: {}", user.getId());
        MessageResponseDto response = passwordService.changePassword(user.getId(), request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot")
    public ResponseEntity<MessageResponseDto> forgotPassword(
            @Valid @RequestBody EmailRequestDto request) {

        log.info("Forgot password request for: {}", request.getEmail());
        MessageResponseDto response = passwordService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/reset")
    public ResponseEntity<MessageResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordDto request) {

        log.info("Password reset request for: {}", request.getEmail());
        MessageResponseDto response = passwordService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}