package com.takeam.userservice.service;

import com.takeam.userservice.dto.response.UserLookupResponseDto;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.mapper.UserLookupMapper;
import com.takeam.userservice.model.User;
import com.takeam.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserLookupMapper userLookupMapper;


    @Transactional(readOnly = true)
    public UserLookupResponseDto getUserByPhoneNumber(String phoneNumber) {

        log.info("Looking up user by phone number: {}", phoneNumber);


        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> {
                    log.error("User not found with phone number: {}", phoneNumber);
                    return new ResourceNotFoundException(
                            "User not found with phone number: " + phoneNumber
                    );
                });

        log.info("User found: {} ({})", user.getFullName(), user.getRole());

        return userLookupMapper.toUserLookupResponse(user);
    }
}
