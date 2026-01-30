package com.takeam.userservice.service;

import com.takeam.userservice.config.JwtUtil;
import com.takeam.userservice.dto.request.UnifiedLoginDto;
import com.takeam.userservice.dto.request.OTPVerificationDto;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.response.TokenResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.exception.UnauthorizedException;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.models.Role;
import com.takeam.userservice.models.User;
import com.takeam.userservice.models.UserStatus;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedAuthService {

    private final UserRepository userRepository;
    private final OTPService otpService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * UNIFIED LOGIN - Handles all user types
     *
     * Traders: Phone + OTP (password is null)
     * Agents/Buyers/Admin: Email + Password
     */
    @Transactional
    public Object login(UnifiedLoginDto request) {
        log.info("Login request for: {}", request.getIdentifier());

        // Determine if it's phone or email
        boolean isPhoneNumber = request.getIdentifier().startsWith("+");

        if (isPhoneNumber) {
            // TRADER LOGIN (OTP-based)
            return handleTraderOTPLogin(request.getIdentifier());
        } else {
            // AGENT/BUYER/ADMIN LOGIN (Password-based)
            return handlePasswordLogin(request.getIdentifier(), request.getPassword());
        }
    }

    /**
     * TRADER LOGIN - Send OTP
     */
    private AuthResponseDto handleTraderOTPLogin(String phoneNumber) {
        log.info("Trader OTP login for: {}", phoneNumber);

        // Find user by phone
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Phone number not registered"));

        // Verify it's a trader
        if (user.getRole() != Role.TRADER) {
            throw new BadRequestException("This phone number is not registered as a trader");
        }

        // Check account status
        validateAccountStatus(user);

        // Generate and send OTP
        String otp = otpService.generateOTP();
        otpService.storeOTP(phoneNumber, otp);
        otpService.sendOTPToPhone(phoneNumber, otp);

        return new AuthResponseDto(
                "OTP sent to your phone number",
                phoneNumber,
                true
        );
    }

    /**
     * AGENT/BUYER/ADMIN LOGIN - Verify Password
     */
    private TokenResponseDto handlePasswordLogin(String email, String password) {
        log.info("Password login for: {}", email);

        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password is required");
        }

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not registered"));

        // Verify NOT a trader
        if (user.getRole() == Role.TRADER) {
            throw new BadRequestException("Traders must login with phone number and OTP");
        }

        // Check account status
        validateAccountStatus(user);

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Password correct - issue JWT
        return generateTokenResponse(user);
    }

    /**
     * VERIFY OTP (for traders)
     */
    @Transactional
    public TokenResponseDto verifyOTP(OTPVerificationDto request) {
        log.info("Verifying OTP for: {}", request.getPhoneNumber());

        // Verify OTP
        boolean isValid = otpService.verifyOTP(request.getPhoneNumber(), request.getOtp());

        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        // Find user
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Issue JWT token
        return generateTokenResponse(user);
    }

    /**
     * Validate account status
     */
    private void validateAccountStatus(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            if (user.getStatus() == UserStatus.PENDING) {
                if (user.getRole() == Role.AGENT) {
                    throw new UnauthorizedException("Your account is pending admin approval");
                }
            }
            throw new UnauthorizedException("Account is " + user.getStatus() + ". Please contact support.");
        }

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException("Account is temporarily locked. Try again later.");
        }
    }

    /**
     * Handle failed login attempts
     */
    private void handleFailedLogin(User user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);

        // Lock account after 5 failed attempts
        if (user.getLoginAttempts() >= 5) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            throw new UnauthorizedException("Too many failed attempts. Account locked for 30 minutes.");
        }

        userRepository.save(user);
    }

    /**
     * Generate JWT token response
     */
    private TokenResponseDto generateTokenResponse(User user) {
        // Reset login attempts on successful login
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateToken(
                user.getId().toString(),
                user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail()
        );

        UserResponseDto userDto = userMapper.toUserResponseDto(user);

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                86400000L,
                userDto
        );
    }
}