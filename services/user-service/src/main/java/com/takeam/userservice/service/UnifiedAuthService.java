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
import com.takeam.userservice.model.Role;
import com.takeam.userservice.model.User;
import com.takeam.userservice.model.UserStatus;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Unified Authentication Service
 *
 * Responsibilities:
 * - Handle login flows (password-based and OTP-based)
 * - Validate user credentials and account status
 * - Manage login attempts and account locking
 * - Orchestrate OTP verification
 * - Delegate token generation to JwtUtil
 *
 * This service handles BUSINESS LOGIC only.
 * Token creation is delegated to JwtUtil (technical responsibility).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedAuthService {

    private final UserRepository userRepository;
    private final OTPService otpService;
    private final JwtUtil jwtUtil;  // Injected utility for token operations
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Unified login endpoint
     *
     * Routes to appropriate login method based on identifier:
     * - Phone number (+234...) → OTP-based login (Traders)
     * - Email → Password-based login (Agents, Buyers, Admins)
     *
     * @param request Login request with identifier and optional password
     * @return Either AuthResponseDto (OTP sent) or TokenResponseDto (logged in)
     */
    @Transactional
    public Object login(UnifiedLoginDto request) {
        log.info("Login request for: {}", request.getIdentifier());

        boolean isPhoneNumber = request.getIdentifier().startsWith("+");

        if (isPhoneNumber) {
            return handleTraderOTPLogin(request.getIdentifier());
        } else {
            return handlePasswordLogin(request.getIdentifier(), request.getPassword());
        }
    }

    /**
     * Handle OTP-based login for traders
     *
     * Flow:
     * 1. Find trader by phone number
     * 2. Validate account status
     * 3. Generate and send OTP
     * 4. Return response indicating OTP was sent
     *
     * @param phoneNumber Trader's phone number
     * @return AuthResponseDto indicating OTP was sent
     */
    private AuthResponseDto handleTraderOTPLogin(String phoneNumber) {
        log.info("Trader OTP login for: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Phone number not registered"));

        if (user.getRole() != Role.TRADER) {
            throw new BadRequestException("This phone number is not registered as a trader");
        }

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
     * Handle password-based login
     *
     * Flow:
     * 1. Find user by email
     * 2. Verify NOT a trader (traders use OTP)
     * 3. Validate account status
     * 4. Verify password
     * 5. Generate and return JWT tokens
     *
     * @param email User's email
     * @param password User's password
     * @return TokenResponseDto with access and refresh tokens
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

        // Password correct - generate and return tokens
        return generateTokenResponse(user);
    }

    /**
     * Verify OTP and issue JWT tokens
     *
     * Flow:
     * 1. Verify OTP is valid
     * 2. Find user by phone number
     * 3. Generate and return JWT tokens
     *
     * @param request OTP verification request
     * @return TokenResponseDto with access and refresh tokens
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

        // Issue JWT tokens
        return generateTokenResponse(user);
    }

    /**
     * Validate user account status
     *
     * Checks:
     * - Account is ACTIVE
     * - Account is not locked
     * - Special handling for pending agents
     *
     * @param user User to validate
     * @throws UnauthorizedException if account is not valid
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
     *
     * Increments login attempts counter.
     * Locks account for 30 minutes after 5 failed attempts.
     *
     * @param user User who failed login
     * @throws UnauthorizedException if account is locked after this attempt
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
     *
     * This method orchestrates token generation by:
     * 1. Updating user state (successful login)
     * 2. Delegating token creation to JwtUtil
     * 3. Building response DTO
     *
     * IMPORTANT: This method does NOT create tokens directly.
     * It delegates to JwtUtil which is the single source of truth for tokens.
     *
     * @param user Authenticated user
     * @return TokenResponseDto containing access token, refresh token, and user info
     */
    private TokenResponseDto generateTokenResponse(User user) {
        // Business Logic: Update user state for successful login
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Delegate token creation to JwtUtil (single responsibility)
        String accessToken = jwtUtil.generateToken(
                user.getId().toString(),  // userId as subject (for authentication.getName())
                user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail(),  // phoneNumber as claim
                user.getRole().name()  // role as claim
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId().toString()  // userId as subject
        );

        // Business Logic: Build response
        UserResponseDto userDto = userMapper.toUserResponseDto(user);

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                86400000L,  // 24 hours in milliseconds
                userDto
        );
    }
}