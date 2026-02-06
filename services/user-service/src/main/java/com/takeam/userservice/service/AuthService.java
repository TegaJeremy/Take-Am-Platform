package com.takeam.userservice.service;

import com.takeam.userservice.config.JwtUtil;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.request.OTPRequestDto;
import com.takeam.userservice.dto.request.OTPVerificationDto;
import com.takeam.userservice.dto.response.TokenResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.model.User;
import com.takeam.userservice.model.UserStatus;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OTPService otpService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public AuthResponseDto requestLoginOTP(OTPRequestDto request) {
        log.info("Login OTP requested for: {}", request.getPhoneNumber());
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Phone number not registered"));

        // 2. Check if account is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Account is " + user.getStatus() + ". Please contact support.");
        }

        // 3. Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Account is temporarily locked. Try again later.");
        }

        // 4. Generate and send OTP
        String otp = otpService.generateOTP();
        otpService.storeOTP(request.getPhoneNumber(), otp);
        otpService.sendOTPToPhone(request.getPhoneNumber(), otp);

        log.info("Login OTP sent to: {}", request.getPhoneNumber());

        return new AuthResponseDto(
                "OTP sent to your phone number",
                request.getPhoneNumber(),
                true
        );
    }

    /**
     * Verify OTP and issue JWT tokens
     */
    @Transactional
    public TokenResponseDto verifyLoginOTP(OTPVerificationDto request) {
        log.info("Verifying login OTP for: {}", request.getPhoneNumber());

        // 1. Find user
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Verify OTP
        boolean isValid = otpService.verifyOTP(request.getPhoneNumber(), request.getOtp());

        if (!isValid) {
            // Increment failed login attempts
            user.setLoginAttempts(user.getLoginAttempts() + 1);

            // Lock account after 5 failed attempts
            if (user.getLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                userRepository.save(user);
                throw new BadRequestException("Too many failed attempts. Account locked for 30 minutes.");
            }

            userRepository.save(user);
            throw new BadRequestException("Invalid or expired OTP");
        }

        // 3. OTP is valid - reset login attempts
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        // 4. Generate JWT tokens
        String accessToken = jwtUtil.generateToken(
                user.getId().toString(),
                user.getPhoneNumber(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getPhoneNumber());

        log.info("Login successful for: {}", request.getPhoneNumber());

        // 5. Build response
        UserResponseDto userDto = userMapper.toUserResponseDto(updatedUser);

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                86400000L,
                userDto
        );
    }

    /**
     * Resend login OTP
     */
    public AuthResponseDto resendLoginOTP(OTPRequestDto request) {
        log.info("Resending login OTP for: {}", request.getPhoneNumber());

        // Check if user exists
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Phone number not registered"));

        // Check if OTP was recently sent (rate limiting)
        if (otpService.hasValidOTP(request.getPhoneNumber())) {
            throw new BadRequestException("OTP already sent. Please wait before requesting again.");
        }

        // Generate and send new OTP
        String otp = otpService.generateOTP();
        otpService.storeOTP(request.getPhoneNumber(), otp);
        otpService.sendOTPToPhone(request.getPhoneNumber(), otp);

        return new AuthResponseDto(
                "OTP resent successfully",
                request.getPhoneNumber(),
                true
        );
    }
}