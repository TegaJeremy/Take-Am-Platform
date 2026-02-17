package com.takeam.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;

    // ─────────────────────────────────────────────
    // OTP NOTIFICATIONS
    // ─────────────────────────────────────────────

    @Async
    public void sendEmailOtp(String email, String otp, String name) {
        log.info("Sending email OTP to: {}", email);
        emailService.sendOTPEmail(email, otp, name);
    }

    @Async
    public void sendSmsOtp(String phoneNumber, String otp) {
        log.info("Sending SMS OTP to: {}", phoneNumber);
        smsService.sendOtpSms(phoneNumber, otp);
    }

    @Async
    public void sendLoginEmailOtp(String email, String otp, String name) {
        log.info("Sending login email OTP to: {}", email);
        emailService.sendLoginOTPEmail(email, otp, name);
    }

    @Async
    public void sendPasswordResetOtp(String email, String otp, String name) {
        log.info("Sending password reset OTP to: {}", email);
        emailService.sendPasswordResetEmail(email, otp, name);
    }

    // ─────────────────────────────────────────────
    // WELCOME NOTIFICATIONS
    // ─────────────────────────────────────────────

    @Async
    public void sendWelcome(String email, String phoneNumber, String name, String role) {
        log.info("Sending welcome notification to: {} / {}", email, phoneNumber);
        if (email != null) {
            emailService.sendWelcomeEmail(email, name, role);
        }
        if (phoneNumber != null) {
            smsService.sendWelcomeSms(phoneNumber, name);
        }
    }

    // ─────────────────────────────────────────────
    // ACCOUNT STATUS NOTIFICATIONS
    // ─────────────────────────────────────────────

    @Async
    public void sendAccountApproved(String email, String phoneNumber, String name) {
        log.info("Sending account approved notification to: {}", email);
        if (email != null) {
            emailService.sendAccountApprovedEmail(email, name);
        }
        if (phoneNumber != null) {
            smsService.sendAccountApprovedSms(phoneNumber, name);
        }
    }

    @Async
    public void sendAccountLocked(String email, String phoneNumber, String name) {
        log.info("Sending account locked notification to: {}", email);
        if (email != null) {
            emailService.sendAccountLockedEmail(email, name);
        }
        if (phoneNumber != null) {
            smsService.sendAccountLockedSms(phoneNumber);
        }
    }

    @Async
    public void sendPasswordChanged(String email, String name) {
        log.info("Sending password changed notification to: {}", email);
        if (email != null) {
            emailService.sendPasswordChangedEmail(email, name);
        }
    }
}