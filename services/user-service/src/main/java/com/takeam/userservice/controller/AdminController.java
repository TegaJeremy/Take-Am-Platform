package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.AdminSeedDto;
import com.takeam.userservice.dto.request.ApprovalActionDto;
import com.takeam.userservice.dto.request.CreateAdminDto;
import com.takeam.userservice.dto.request.UserActionDto;
import com.takeam.userservice.dto.response.*;
import com.takeam.userservice.model.AdminAuditLog;
import com.takeam.userservice.model.Role;
import com.takeam.userservice.model.User;
import com.takeam.userservice.model.UserStatus;
import com.takeam.userservice.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;


    @PostMapping("/seed")
    public ResponseEntity<MessageResponseDto> seedSuperAdmin(
            @Valid @RequestBody AdminSeedDto request) {

        log.info("Super Admin seed request");
        MessageResponseDto response = adminService.seedSuperAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponseDto> createAdmin(
            @AuthenticationPrincipal User superAdmin,
            @Valid @RequestBody CreateAdminDto request) {

        log.info("Creating admin by Super Admin: {}", superAdmin.getId());
        UserResponseDto response = adminService.createAdmin(superAdmin.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/agents/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AgentDetailDto>> getPendingAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AgentDetailDto> response = adminService.getPendingAgents(pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/agents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AgentDetailDto> getAgentDetails(@PathVariable UUID id) {
        AgentDetailDto response = adminService.getAgentDetails(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/agents/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> approveAgent(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalActionDto request) {

        MessageResponseDto response = adminService.approveAgent(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/agents/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> rejectAgent(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalActionDto request) {

        MessageResponseDto response = adminService.rejectAgent(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    // user management


    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> response = adminService.getAllUsers(role, status, pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponseDto> getUserDetails(@PathVariable UUID id) {
        UserResponseDto response = adminService.getUserDetails(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/users/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> suspendUser(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody UserActionDto request) {

        MessageResponseDto response = adminService.suspendUser(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/users/{id}/ban")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> banUser(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody UserActionDto request) {

        MessageResponseDto response = adminService.banUser(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/users/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> reactivateUser(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody UserActionDto request) {

        MessageResponseDto response = adminService.reactivateUser(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    // dashboards and stats


    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminAuditLog> response = adminService.getAuditLogs(pageable);
        return ResponseEntity.ok(response);
    }
}