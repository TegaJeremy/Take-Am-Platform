package com.takeam.userservice.controller;

import com.takeam.userservice.dto.request.AdminSeedDto;
import com.takeam.userservice.dto.request.ApprovalActionDto;
import com.takeam.userservice.dto.request.CreateAdminDto;
import com.takeam.userservice.dto.request.UserActionDto;
import com.takeam.userservice.dto.response.*;
import com.takeam.userservice.models.AdminAuditLog;
import com.takeam.userservice.models.Role;
import com.takeam.userservice.models.User;
import com.takeam.userservice.models.UserStatus;
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

    // ============ ADMIN CREATION ============

    /**
     * Seed Super Admin (public - run once only)
     * POST /api/v1/admin/seed
     */
    @PostMapping("/seed")
    public ResponseEntity<MessageResponseDto> seedSuperAdmin(
            @Valid @RequestBody AdminSeedDto request) {

        log.info("Super Admin seed request");
        MessageResponseDto response = adminService.seedSuperAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Create Admin (Super Admin only)
     * POST /api/v1/admin/create
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponseDto> createAdmin(
            @AuthenticationPrincipal User superAdmin,
            @Valid @RequestBody CreateAdminDto request) {

        log.info("Creating admin by Super Admin: {}", superAdmin.getId());
        UserResponseDto response = adminService.createAdmin(superAdmin.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ============ AGENT MANAGEMENT ============

    /**
     * Get pending agents
     * GET /api/v1/admin/agents/pending?page=0&size=20
     */
    @GetMapping("/agents/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AgentDetailDto>> getPendingAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AgentDetailDto> response = adminService.getPendingAgents(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get agent details
     * GET /api/v1/admin/agents/{id}
     */
    @GetMapping("/agents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<AgentDetailDto> getAgentDetails(@PathVariable UUID id) {
        AgentDetailDto response = adminService.getAgentDetails(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Approve agent
     * POST /api/v1/admin/agents/{id}/approve
     */
    @PostMapping("/agents/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> approveAgent(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalActionDto request) {

        MessageResponseDto response = adminService.approveAgent(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject agent
     * POST /api/v1/admin/agents/{id}/reject
     */
    @PostMapping("/agents/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> rejectAgent(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalActionDto request) {

        MessageResponseDto response = adminService.rejectAgent(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    // ============ USER MANAGEMENT ============

    /**
     * Get all users
     * GET /api/v1/admin/users?role=TRADER&status=ACTIVE&page=0&size=20
     */
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

    /**
     * Get user details
     * GET /api/v1/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponseDto> getUserDetails(@PathVariable UUID id) {
        UserResponseDto response = adminService.getUserDetails(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Suspend user
     * POST /api/v1/admin/users/{id}/suspend
     */
    @PostMapping("/users/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> suspendUser(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody UserActionDto request) {

        MessageResponseDto response = adminService.suspendUser(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Ban user
     * POST /api/v1/admin/users/{id}/ban
     */
    @PostMapping("/users/{id}/ban")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> banUser(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody UserActionDto request) {

        MessageResponseDto response = adminService.banUser(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reactivate user
     * POST /api/v1/admin/users/{id}/reactivate
     */
    @PostMapping("/users/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MessageResponseDto> reactivateUser(
            @AuthenticationPrincipal User admin,
            @PathVariable UUID id,
            @Valid @RequestBody UserActionDto request) {

        MessageResponseDto response = adminService.reactivateUser(admin.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    // ============ DASHBOARD & STATS ============

    /**
     * Get dashboard statistics
     * GET /api/v1/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit logs
     * GET /api/v1/admin/audit-logs?page=0&size=50
     */
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