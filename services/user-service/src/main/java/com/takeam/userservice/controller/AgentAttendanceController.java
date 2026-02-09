package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.ClockInRequest;
import com.takeam.userservice.dto.request.ClockOutRequest;
import com.takeam.userservice.dto.response.AttendanceResponse;
import com.takeam.userservice.model.User;
import com.takeam.userservice.service.AgentAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
            @AuthenticationPrincipal User user
    ) {
        UUID agentId = user.getId();
        log.info("Clock-in request from agent: {}", agentId);

        AttendanceResponse response = attendanceService.clockIn(agentId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(
            @Valid @RequestBody ClockOutRequest request,
            @AuthenticationPrincipal User user
    ) {
        UUID agentId = user.getId();
        log.info("Clock-out request from agent: {}", agentId);

        AttendanceResponse response = attendanceService.clockOut(agentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<AttendanceResponse> getStatus(@AuthenticationPrincipal User user) {
        UUID agentId = user.getId();
        log.info("📊 Getting attendance status for agent: {}", agentId);

        AttendanceResponse response = attendanceService.getClockInStatus(agentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceResponse>> getHistory(
            @RequestParam(defaultValue = "30") int limit,
            @AuthenticationPrincipal User user
    ) {
        UUID agentId = user.getId();
        log.info("Attendance history request for agent: {}, limit: {}", agentId, limit);

        List<AttendanceResponse> history = attendanceService.getAttendanceHistory(agentId, limit);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/is-clocked-in")
    public ResponseEntity<Map<String, Boolean>> isClockedIn(@AuthenticationPrincipal User user) {
        UUID agentId = user.getId();
        log.info("Checking clock-in status for agent: {}", agentId);

        boolean isClockedIn = attendanceService.isAgentClockedIn(agentId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("clockedIn", isClockedIn);

        return ResponseEntity.ok(response);
    }
}