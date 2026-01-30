package com.takeam.userservice.service;

import com.takeam.userservice.dto.request.ChangePhoneNumberDto;
import com.takeam.userservice.dto.request.OTPVerificationDto;
import com.takeam.userservice.dto.request.TraderRegistrationRequestDto;
import com.takeam.userservice.dto.request.UpdateTraderProfileDto;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.response.TraderDetailResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.mapper.TraderMapper;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.models.Trader;
import com.takeam.userservice.models.User;
import com.takeam.userservice.models.UserStatus;
import com.takeam.userservice.repository.TraderRepository;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraderService {

    private final UserRepository userRepository;
    private final TraderRepository traderRepository;
    private final OTPService otpService;
    private final TraderMapper traderMapper;
    private final UserMapper userMapper;

    //register
    @Transactional
    public AuthResponseDto registerTrader(TraderRegistrationRequestDto dto) {
        log.info("Starting trader registration for phone: {}", dto.getPhoneNumber());

        validatePhoneNumberNotExists(dto.getPhoneNumber());

        User user = createAndSaveUser(dto);
        createAndSaveTrader(dto, user);
        sendRegistrationOTP(dto.getPhoneNumber());

        return new AuthResponseDto(
                "Registration successful! OTP sent to your phone number.",
                dto.getPhoneNumber(),
                true
        );
    }

    private void validatePhoneNumberNotExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BadRequestException("Phone number already registered");
        }
    }

    private User createAndSaveUser(TraderRegistrationRequestDto dto) {
        User user = traderMapper.toUser(dto);
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return savedUser;
    }

    private void createAndSaveTrader(TraderRegistrationRequestDto dto, User user) {
        Trader trader = traderMapper.toTrader(dto);
        trader.setUser(user);
        traderRepository.save(trader);
        log.info("Trader profile created for user: {}", user.getId());
    }

    private void sendRegistrationOTP(String phoneNumber) {
        String otp = otpService.generateOTP();
        otpService.storeOTP(phoneNumber, otp);
        otpService.sendOTPToPhone(phoneNumber, otp);
    }



    @Transactional
    public UserResponseDto verifyOTP(OTPVerificationDto dto) {
        log.info("Verifying OTP for phone: {}", dto.getPhoneNumber());

        validateOTP(dto.getPhoneNumber(), dto.getOtp());

        User user = findUserByPhone(dto.getPhoneNumber());
        activateUser(user);
        verifyTrader(user.getId());

        return userMapper.toUserResponseDto(user);
    }

    private void validateOTP(String phoneNumber, String otp) {
        boolean isValid = otpService.verifyOTP(phoneNumber, otp);
        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP");
        }
    }

    private User findUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void activateUser(User user) {
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Trader account activated: {}", user.getId());
    }

    private void verifyTrader(UUID userId) {
        Trader trader = traderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader profile not found"));
        trader.setVerified(true);
        traderRepository.save(trader);
    }



    public AuthResponseDto resendOTP(String phoneNumber) {
        log.info("Resending OTP to phone: {}", phoneNumber);

        User user = findUserByPhone(phoneNumber);
        validateUserNotActive(user);
        validateNoRecentOTP(phoneNumber);
        sendRegistrationOTP(phoneNumber);

        return new AuthResponseDto(
                "OTP resent successfully!",
                phoneNumber,
                true
        );
    }

    private void validateUserNotActive(User user) {
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("Account already verified");
        }
    }

    private void validateNoRecentOTP(String phoneNumber) {
        if (otpService.hasValidOTP(phoneNumber)) {
            throw new BadRequestException("OTP already sent. Please wait before requesting again.");
        }
    }



    public TraderDetailResponseDto getTraderDetails(UUID userId) {
        log.info("Fetching trader details for user: {}", userId);

        Trader trader = traderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found"));

        return traderMapper.toDetailResponse(trader);
    }



    @Transactional
    public TraderDetailResponseDto updateProfile(UUID userId, UpdateTraderProfileDto dto) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Trader trader = traderRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader not found"));

        updateUserInfo(user, dto);
        updateTraderInfo(trader, dto);

        return traderMapper.toDetailResponse(trader);
    }

    private void updateUserInfo(User user, UpdateTraderProfileDto dto) {
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
            userRepository.save(user);
        }
    }

    private void updateTraderInfo(Trader trader, UpdateTraderProfileDto dto) {
//        if (dto.getStallNumber() != null) {
//            trader.setStallNumber(dto.getStallNumber());
//        }
        if (dto.getBankAccountNumber() != null) {
            trader.setBankAccountNumber(dto.getBankAccountNumber());
        }
//        if (dto.getBankCode() != null) {
//            trader.setBankCode(dto.getBankCode());
//        }
        if (dto.getBankName() != null) {
            trader.setBankName(dto.getBankName());
        }
        if (dto.getAccountName() != null) {
            trader.setAccountName(dto.getAccountName());
        }

        traderRepository.save(trader);
    }

    // ============ CHANGE PHONE NUMBER ============

    @Transactional
    public UserResponseDto changePhoneNumber(UUID userId, ChangePhoneNumberDto dto) {
        log.info("Changing phone number for user: {}", userId);

        validateOTP(dto.getNewPhoneNumber(), dto.getOtp());
        validatePhoneNumberNotExists(dto.getNewPhoneNumber());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPhoneNumber(dto.getNewPhoneNumber());
        User updatedUser = userRepository.save(user);

        log.info("Phone number changed successfully for user: {}", userId);
        return userMapper.toUserResponseDto(updatedUser);
    }



    @Transactional
    public void deactivateAccount(UUID userId) {
        log.info("Deactivating account for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        log.info("Account deactivated: {}", userId);
    }
}