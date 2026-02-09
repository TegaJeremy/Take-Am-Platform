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
    private final JwtUtil jwtUtil;  // Injected utility for token operations
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


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


    private AuthResponseDto handleTraderOTPLogin(String phoneNumber) {
        log.info("Trader OTP login for: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Phone number not registered"));

        if (user.getRole() != Role.TRADER) {
            throw new BadRequestException("This phone number is not registered as a trader");
        }

        validateAccountStatus(user);

        String otp = otpService.generateOTP();
        otpService.storeOTP(phoneNumber, otp);
        otpService.sendOTPToPhone(phoneNumber, otp);

        return new AuthResponseDto(
                "OTP sent to your phone number",
                phoneNumber,
                true,
                otp
        );
    }


    private TokenResponseDto handlePasswordLogin(String email, String password) {
        log.info("Password login for: {}", email);

        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password is required");
        }


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not registered"));

        // Verify NOT a trader
        if (user.getRole() == Role.TRADER) {
            throw new BadRequestException("Traders must login with phone number and OTP");
        }


        validateAccountStatus(user);


        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        return generateTokenResponse(user);
    }


    @Transactional
    public TokenResponseDto verifyOTP(OTPVerificationDto request) {
        log.info("Verifying OTP for: {}", request.getPhoneNumber());

        // Verify OTP
        boolean isValid = otpService.verifyOTP(request.getPhoneNumber(), request.getOtp());

        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP");
        }


        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        return generateTokenResponse(user);
    }


    private void validateAccountStatus(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            if (user.getStatus() == UserStatus.PENDING) {
                if (user.getRole() == Role.AGENT) {
                    throw new UnauthorizedException("Your account is pending admin approval");
                }
            }
            throw new UnauthorizedException("Account is " + user.getStatus() + ". Please contact support.");
        }


        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException("Account is temporarily locked. Try again later.");
        }
    }


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