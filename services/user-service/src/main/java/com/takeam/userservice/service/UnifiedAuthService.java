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
     * Unified login entry point
     */
    @Transactional
    public Object login(UnifiedLoginDto request) {
        log.info("Login attempt for identifier: {}", request.getIdentifier());

        if (request.getIdentifier() == null || request.getIdentifier().isBlank()) {
            throw new BadRequestException("Login identifier is required");
        }

        boolean isPhoneNumber = request.getIdentifier().startsWith("+");

        return isPhoneNumber
                ? handleTraderOTPLogin(request.getIdentifier())
                : handlePasswordLogin(request.getIdentifier(), request.getPassword());
    }

    /**
     * Trader login via OTP
     */
    private AuthResponseDto handleTraderOTPLogin(String phoneNumber) {
        log.info("OTP login requested for phone number: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Phone number is not registered"));

        if (user.getRole() != Role.TRADER) {
            throw new BadRequestException("Only traders can log in with phone number");
        }

        validateAccountStatus(user);

        String otp = otpService.generateOTP();
        otpService.storeOTP(phoneNumber, otp);
        otpService.sendOTPToPhone(phoneNumber, otp);

        return new AuthResponseDto(
                "An OTP has been sent to your phone number",
                phoneNumber,
                true,
                otp
        );
    }

    /**
     * Email + password login (non-traders)
     */
    private TokenResponseDto handlePasswordLogin(String email, String password) {
        log.info("Password login attempt for email: {}", email);

        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email is not registered"));

        if (user.getRole() == Role.TRADER) {
            throw new BadRequestException("Traders must log in using phone number and OTP");
        }

        validateAccountStatus(user);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid password");
        }

        return generateTokenResponse(user);
    }

    /**
     * OTP verification
     */
    @Transactional
    public TokenResponseDto verifyOTP(OTPVerificationDto request) {
        log.info("OTP verification for phone number: {}", request.getPhoneNumber());

        boolean isValid = otpService.verifyOTP(
                request.getPhoneNumber(),
                request.getOtp()
        );

        if (!isValid) {
            throw new BadRequestException("The OTP you entered is invalid or expired");
        }

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        return generateTokenResponse(user);
    }

    /**
     * Account status validation
     */
    private void validateAccountStatus(User user) {

        if (user.getStatus() != UserStatus.ACTIVE) {
            if (user.getStatus() == UserStatus.PENDING && user.getRole() == Role.AGENT) {
                throw new UnauthorizedException("Your account is awaiting admin approval");
            }
            throw new UnauthorizedException("Your account is not active. Please contact support");
        }

        if (user.getLockedUntil() != null &&
                user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException(
                    "Your account is temporarily locked. Please try again later"
            );
        }
    }

    /**
     * Failed password attempt handler
     */
    private void handleFailedLogin(User user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);

        if (user.getLoginAttempts() >= 5) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            throw new UnauthorizedException(
                    "Too many failed attempts. Your account is locked for 30 minutes"
            );
        }

        userRepository.save(user);
    }

    /**
     * Token generation
     */
    private TokenResponseDto generateTokenResponse(User user) {

        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(
                user.getId().toString(),
                user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId().toString()
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


//Retrieve SMTP credentials
//This is the only time you can view and download these SMTP security credentials. Never share your SMTP credentials with anyone.
//
//        SMTP credentials
//IAM user name
//
//ses-smtp-user.20260220-232608
//SMTP user name
//
//        AKIAZI2LCGLLUJTDKHXE
//SMTP password
//
//BLrxafqepyIJQAn9xL67PSINY9DKAVNohHJirxGuP4GQ
//        Hide
