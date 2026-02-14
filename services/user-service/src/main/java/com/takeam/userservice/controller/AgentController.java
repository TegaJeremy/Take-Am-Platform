package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.AgentRegistrationRequestDto;
import com.takeam.userservice.dto.request.AgentVerifyOTPDto;
import com.takeam.userservice.dto.request.TraderRegistrationRequestDto;
import com.takeam.userservice.dto.response.AgentDetailDto;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.model.Trader;
import com.takeam.userservice.model.User;
import com.takeam.userservice.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AgentService agentService;

    //public

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerAgent(
            @Valid @RequestBody AgentRegistrationRequestDto request) {

        AuthResponseDto response = agentService.registerAgent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<UserResponseDto> verifyOTP(
            @Valid @RequestBody AgentVerifyOTPDto request) {

        UserResponseDto response = agentService.verifyOTP(request);
        return ResponseEntity.ok(response);
    }

    // protected agents only

    @GetMapping("/details")
    public ResponseEntity<AgentDetailDto> getAgentDetails(
            @AuthenticationPrincipal User user) {

        AgentDetailDto response = agentService.getAgentDetails(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentDetailDto> getAgentById(@PathVariable UUID id) {
        log.info("Fetching agent details for ID: {}", id);
        AgentDetailDto response = agentService.getAgentDetailsById(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register-trader")
    public ResponseEntity<AuthResponseDto> registerTraderOnBehalf(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TraderRegistrationRequestDto request) { // ← Same DTO!

        log.info("Agent {} registering trader", user.getId());
        AuthResponseDto response = agentService.registerTraderOnBehalf(
                user.getId(),
                request
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my-traders")
    public ResponseEntity<List<Trader>> getTradersRegisteredByMe(
            @AuthenticationPrincipal User user) {

        List<Trader> traders = agentService.getTradersRegisteredByAgent(user.getId());
        return ResponseEntity.ok(traders);
    }
}