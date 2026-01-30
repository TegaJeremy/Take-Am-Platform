package com.takeam.userservice.service;

import com.takeam.userservice.dto.request.BuyerRegistrationDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.mapper.BuyerMapper;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.models.Buyer;
import com.takeam.userservice.models.User;
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
public class BuyerService {

    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final BuyerMapper buyerMapper;  // ← MapStruct mapper
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto registerBuyer(BuyerRegistrationDto dto) {
        log.info("Registering buyer: {}", dto.getEmail());

        // 1. Validate
        validateEmailNotExists(dto.getEmail());
        validatePhoneNotExists(dto.getPhoneNumber());

        // 2. Use MapStruct to create User
        User user = buyerMapper.toUser(dto);

        // 3. Hash password (MapStruct can't do this - security concern!)
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());

        // 4. Use MapStruct to create Buyer
        Buyer buyer = buyerMapper.toBuyer(dto);
        buyer.setUser(savedUser);

        buyerRepository.save(buyer);
        log.info("Buyer profile created for user: {}", savedUser.getId());

        return userMapper.toUserResponseDto(savedUser);
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