package com.takeam.userservice.service;

import com.takeam.userservice.config.JwtUtil;
import com.takeam.userservice.dto.request.BuyerRegistrationDto;
import com.takeam.userservice.dto.request.OTPVerificationDto;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.response.TokenResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.mapper.BuyerMapper;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.model.Buyer;
import com.takeam.userservice.model.User;
import com.takeam.userservice.model.UserStatus;
import com.takeam.userservice.repository.BuyerRepository;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class   BuyerService {

    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final BuyerMapper buyerMapper;
    private final UserMapper userMapper;
    private final OTPService otpService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponseDto registerBuyer(BuyerRegistrationDto dto) {
        log.info("Registering buyer with email: {}", dto.getEmail());

        validateEmailNotExists(dto.getEmail());
        validatePhoneNotExists(dto.getPhoneNumber());

        User user = buyerMapper.toUser(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(UserStatus.PENDING);

        User savedUser = userRepository.save(user);

        Buyer buyer = buyerMapper.toBuyer(dto);
        buyer.setUser(savedUser);
        buyerRepository.save(buyer);

        String otp = generateAndSendEmailOTP(dto.getEmail(), user.getFullName());

        return new AuthResponseDto(
                "Registration successful! OTP sent to your email.",
                dto.getEmail(),
                true,
                otp
        );
    }

    // verify otp

    @Transactional
    public TokenResponseDto verifyEmailOTP(OTPVerificationDto dto) {
        log.info("Verifying buyer email OTP for: {}", dto.getEmail());

        boolean isValid = otpService.verifyOTP(dto.getEmail(), dto.getOtp());
        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(UserStatus.ACTIVE);
        user.setVerified(true);
        userRepository.save(user);

        Buyer buyer = buyerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found"));

        buyer.setEmailVerified(true);
        buyerRepository.save(buyer);

        return generateTokenResponse(user);
    }



    public AuthResponseDto resendEmailOTP(String email) {
        log.info("Resending OTP to email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("Account already verified");
        }

        if (otpService.hasValidOTP(email)) {
            throw new BadRequestException("OTP already sent. Please wait before requesting again.");
        }

        generateAndSendEmailOTP(email, user.getFullName());

        return new AuthResponseDto(
                "OTP resent successfully!",
                email,
                true,
                null
        );
    }



    private String generateAndSendEmailOTP(String email, String fullName) {
        String otp = otpService.generateOTP();
        otpService.storeOTP(email, otp);
        emailService.sendOTPEmail(email, otp, fullName);
        return otp;
    }

    private TokenResponseDto generateTokenResponse(User user) {
        String accessToken = jwtUtil.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                86400000L,
                userMapper.toUserResponseDto(user)
        );
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
    }

    private void validatePhoneNotExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BadRequestException("Phone number already registered");
        }
    }
}
