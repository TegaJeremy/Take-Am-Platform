package com.takeam.userservice.service;

import com.takeam.userservice.dto.request.ChangePasswordDto;
import com.takeam.userservice.dto.request.EmailRequestDto;
import com.takeam.userservice.dto.request.ResetPasswordDto;
import com.takeam.userservice.dto.response.MessageResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.exception.UnauthorizedException;
import com.takeam.userservice.model.Role;
import com.takeam.userservice.model.User;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final EmailService emailService;


    @Transactional
    public MessageResponseDto changePassword(UUID userId, ChangePasswordDto dto) {
        log.info("Password change request for user: {}", userId);


        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        if (user.getPasswordHash() == null) {
            throw new BadRequestException("Your account uses OTP login, no password to change");
        }


        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }


        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }


        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);

        return new MessageResponseDto(
                "Password changed successfully",
                true
        );
    }


    public MessageResponseDto forgotPassword(EmailRequestDto dto) {
        log.info("Forgot password request for email: {}", dto.getEmail());


        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email not registered"));


        if (user.getRole() == Role.TRADER) {
            throw new BadRequestException("Traders use OTP login, password reset not applicable");
        }


        String otp = otpService.generateOTP();


        otpService.storeOTP(dto.getEmail(), otp);

        // 5. Send OTP via email
        otpService.sendOTPToEmail(
                dto.getEmail(),
                otp,
                user.getFullName() + " (Password Reset)"
        );

        log.info("Password reset OTP sent to: {}", dto.getEmail());

        return new MessageResponseDto(
                "OTP sent to your email address. Valid for 5 minutes.",
                true
        );
    }


    @Transactional
    public MessageResponseDto resetPassword(ResetPasswordDto dto) {
        log.info("Password reset request for email: {}", dto.getEmail());


        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }


        boolean isValid = otpService.verifyOTP(dto.getEmail(), dto.getOtp());
        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP");
        }


        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));


        user.setLoginAttempts(0);
        user.setLockedUntil(null);

        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getId());

        return new MessageResponseDto(
                "Password reset successfully. You can now login with your new password.",
                true
        );
    }
}