package com.takeam.userservice.controller;


import com.takeam.userservice.dto.response.UserLookupResponseDto;
import com.takeam.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService authService;

    @GetMapping("/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserByPhoneNumber(
            @PathVariable String phoneNumber
    ) {
        log.info("Received request to lookup user by phone: {}", phoneNumber);

        UserLookupResponseDto userLookup = authService.getUserByPhoneNumber(phoneNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User found successfully");
        response.put("data", userLookup);

        log.info("Successfully retrieved user: {}", userLookup.getFullName());

        return ResponseEntity.ok(response);
    }
}
