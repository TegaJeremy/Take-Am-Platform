package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.ClockInRequest;
import com.takeam.userservice.dto.request.ClockOutRequest;
import com.takeam.userservice.dto.response.AttendanceResponse;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.model.Agent;
import com.takeam.userservice.model.User;
import com.takeam.userservice.repository.UserRepository;
import com.takeam.userservice.service.AgentAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/agents/attendance")
@RequiredArgsConstructor
@Slf4j
public class AgentAttendanceController {

    private final AgentAttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(
            @Valid @RequestBody ClockInRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        UUID agentId = user.getId();
//        UUID agentId = UUID.fromString(authentication.getName());
        log.info("Clock-in request from agent: {}", agentId);

        AttendanceResponse response = attendanceService.clockIn(agentId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(
            @Valid @RequestBody ClockOutRequest request,
            Authentication authentication
    ) {
        UUID agentId = UUID.fromString(authentication.getName());
        log.info("Clock-out request from agent: {}", agentId);

        AttendanceResponse response = attendanceService.clockOut(agentId, request);
        return ResponseEntity.ok(response);
    }


//    @GetMapping("/status")
//    public ResponseEntity<?> getStatus(Principal principal) {
//        log.info("📊 Getting status for: {}", principal.getName());
//
//
//        String phoneNumber = principal.getName();
//
//
//
//        Agent agent = agentRepository.findByPhoneNumber(phoneNumber)
//                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
//
//
//        return ResponseEntity.ok(Map.of(
//                "agentId", agent.getId(),
//                "phoneNumber", agent.getPhoneNumber(),
//                "firstName", agent.getFirstName(),
//                "lastName", agent.getLastName(),
//                "status", agent.getStatus(),
//                "isActive", agent.getStatus() == AgentStatus.ACTIVE
//        ));
//    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceResponse>> getHistory(
            @RequestParam(defaultValue = "30") int limit,
            Authentication authentication
    ) {
        UUID agentId = UUID.fromString(authentication.getName());
        log.info("Attendance history request for agent: {}, limit: {}", agentId, limit);

        List<AttendanceResponse> history = attendanceService.getAttendanceHistory(agentId, limit);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/is-clocked-in")
    public ResponseEntity<Boolean> isClockedIn(Authentication authentication) {
        UUID agentId = UUID.fromString(authentication.getName());
        boolean isClockedIn = attendanceService.isAgentClockedIn(agentId);
        return ResponseEntity.ok(isClockedIn);
    }
}