package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.AgentRegistrationRequestDto;
import com.takeam.userservice.dto.request.AgentVerifyOTPDto;
import com.takeam.userservice.dto.request.TraderRegistrationRequestDto; // ← Same DTO!
import com.takeam.userservice.dto.response.AgentDetailResponseDto;
import com.takeam.userservice.dto.response.AuthResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.models.Trader;
import com.takeam.userservice.models.User;
import com.takeam.userservice.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AgentService agentService;

    // ========== PUBLIC ENDPOINTS ==========

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

    // ========== PROTECTED ENDPOINTS (AGENT ONLY) ==========

//    @GetMapping("/details")
//    public ResponseEntity<AgentDetailResponseDto> getAgentDetails(
//            @AuthenticationPrincipal User user) {
//
//        AgentDetailResponseDto response = agentService.getAgentDetails(user.getId());
//        return ResponseEntity.ok(response);
//    }

    /**
     * Agent registers trader on their behalf
     * Uses the SAME DTO as normal trader registration!
     */
//    @PostMapping("/register-trader")
//    public ResponseEntity<AuthResponseDto> registerTraderOnBehalf(
//            @AuthenticationPrincipal User user,
//            @Valid @RequestBody TraderRegistrationRequestDto request) { // ← Same DTO!
//
//        log.info("Agent {} registering trader", user.getId());
//        AuthResponseDto response = agentService.registerTraderOnBehalf(
//                user.getId(),
//                request
//        );
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }

    @GetMapping("/my-traders")
    public ResponseEntity<List<Trader>> getTradersRegisteredByMe(
            @AuthenticationPrincipal User user) {

        List<Trader> traders = agentService.getTradersRegisteredByAgent(user.getId());
        return ResponseEntity.ok(traders);
    }
}