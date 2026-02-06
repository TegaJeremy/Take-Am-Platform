package com.takeam.userservice.service;

import com.takeam.userservice.dto.request.AdminSeedDto;
import com.takeam.userservice.dto.request.ApprovalActionDto;
import com.takeam.userservice.dto.request.CreateAdminDto;
import com.takeam.userservice.dto.request.UserActionDto;
import com.takeam.userservice.dto.response.AgentDetailDto;
import com.takeam.userservice.dto.response.DashboardStatsDto;
import com.takeam.userservice.dto.response.MessageResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.exception.BadRequestException;
import com.takeam.userservice.exception.ResourceNotFoundException;
import com.takeam.userservice.exception.UnauthorizedException;
import com.takeam.userservice.mapper.UserMapper;
import com.takeam.userservice.mapper.AgentMapper;
import com.takeam.userservice.model.*;
import com.takeam.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final TraderRepository traderRepository;
    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AdminAuditService auditService;
    private final EmailService emailService;
    private final AgentMapper agentMapper;

    // ============ ADMIN CREATION ============

    /**
     * Seed initial Super Admin (run once)
     */
    @Transactional
    public MessageResponseDto seedSuperAdmin(AdminSeedDto dto) {
        log.info("Attempting to seed Super Admin");

        // Check if ANY admin exists
        if (userRepository.existsByRole(Role.ADMIN) || userRepository.existsByRole(Role.SUPER_ADMIN)) {
            throw new BadRequestException("Admin already exists. Cannot seed again.");
        }

        // Validate email not taken
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Create Super Admin
        User admin = new User();
        admin.setEmail(dto.getEmail());
        admin.setFullName(dto.getFullName());
        admin.setPhoneNumber(dto.getPhoneNumber());
        admin.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(dto.isSuperAdmin() ? Role.SUPER_ADMIN : Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setLoginAttempts(0);

        userRepository.save(admin);

        log.info("Super Admin created: {}", admin.getEmail());

        return new MessageResponseDto(
                "Super Admin created successfully. Email: " + admin.getEmail(),
                true
        );
    }

    /**
     * Create Admin (by Super Admin only)
     */
    @Transactional
    public UserResponseDto createAdmin(UUID superAdminId, CreateAdminDto dto) {
        log.info("Creating admin by Super Admin: {}", superAdminId);

        // Verify requester is Super Admin
        User superAdmin = userRepository.findById(superAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Super Admin not found"));

        if (superAdmin.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only Super Admin can create admins");
        }

        // Validate email not taken
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new BadRequestException("Phone number already registered");
        }

        // Create Admin
        User admin = new User();
        admin.setEmail(dto.getEmail());
        admin.setFullName(dto.getFullName());
        admin.setPhoneNumber(dto.getPhoneNumber());
        admin.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setLoginAttempts(0);

        User savedAdmin = userRepository.save(admin);

        // Log action
        auditService.logAction(
                superAdminId,
                superAdmin.getEmail(),
                "CREATE_ADMIN",
                savedAdmin.getId(),
                savedAdmin.getEmail(),
                "New admin created",
                null,
                null
        );

        log.info("Admin created: {}", savedAdmin.getEmail());

        return userMapper.toUserResponseDto(savedAdmin);
    }

    // ============ AGENT APPROVAL ============

    /**
     * Get all pending agents
     */
    public Page<AgentDetailDto> getPendingAgents(Pageable pageable) {
        log.info("Fetching pending agents");

        Page<Agent> agents = agentRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable);

        return agents.map(this::mapToAgentDetailDto);
    }

    /**
     * Get agent details
     */
    public AgentDetailDto getAgentDetails(UUID agentId) {
        log.info("Fetching agent details: {}", agentId);

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        return mapToAgentDetailDto(agent);
    }

    /**
     * Approve agent
     */
    @Transactional
    public MessageResponseDto approveAgent(UUID adminId, UUID agentId, ApprovalActionDto dto) {
        log.info("Admin {} approving agent {}", adminId, agentId);

        // Get admin
        User admin = getAdminUser(adminId);

        // Get agent
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        // Check if already approved
        if (agent.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new BadRequestException("Agent is already approved");
        }

        // Approve
        agent.setApprovalStatus(ApprovalStatus.APPROVED);
        agent.setApprovedBy(adminId);
        agent.setApprovedAt(LocalDateTime.now());
        agentRepository.save(agent);

        // Activate user account
        User agentUser = agent.getUser();
        agentUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(agentUser);

        // Log action
        auditService.logAction(
                adminId,
                admin.getEmail(),
                "APPROVE_AGENT",
                agentUser.getId(),
                agentUser.getEmail(),
                dto.getReason(),
                dto.getNotes(),
                null
        );

        // Send email notification
        emailService.sendOTPEmail(
                agentUser.getEmail(),
                "Your agent account has been approved! You can now login and start working.",
                agentUser.getFullName()
        );

        log.info("Agent approved: {}", agentId);

        return new MessageResponseDto(
                "Agent approved successfully",
                true
        );
    }

    /**
     * Reject agent
     */
    @Transactional
    public MessageResponseDto rejectAgent(UUID adminId, UUID agentId, ApprovalActionDto dto) {
        log.info("Admin {} rejecting agent {}", adminId, agentId);

        // Get admin
        User admin = getAdminUser(adminId);

        // Get agent
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        // Reject
        agent.setApprovalStatus(ApprovalStatus.REJECTED);
        agent.setRejectionReason(dto.getReason());
        agentRepository.save(agent);

        // Keep user status as PENDING (they can reapply later)
        User agentUser = agent.getUser();

        // Log action
        auditService.logAction(
                adminId,
                admin.getEmail(),
                "REJECT_AGENT",
                agentUser.getId(),
                agentUser.getEmail(),
                dto.getReason(),
                dto.getNotes(),
                null
        );

        // Send email notification
        emailService.sendOTPEmail(
                agentUser.getEmail(),
                "Your agent application has been rejected. Reason: " + dto.getReason(),
                agentUser.getFullName()
        );

        log.info("Agent rejected: {}", agentId);

        return new MessageResponseDto(
                "Agent rejected successfully",
                true
        );
    }

    // ============ USER MANAGEMENT ============

    /**
     * Get all users (with filters)
     */
    public Page<UserResponseDto> getAllUsers(Role role, UserStatus status, Pageable pageable) {
        log.info("Fetching users - Role: {}, Status: {}", role, status);

        Page<User> users;

        if (role != null && status != null) {
            users = userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(userMapper::toUserResponseDto);
    }

    /**
     * Get user details
     */
    public UserResponseDto getUserDetails(UUID userId) {
        log.info("Fetching user details: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toUserResponseDto(user);
    }

    /**
     * Suspend user
     */
    @Transactional
    public MessageResponseDto suspendUser(UUID adminId, UUID userId, UserActionDto dto) {
        log.info("Admin {} suspending user {}", adminId, userId);

        // Get admin
        User admin = getAdminUser(adminId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Cannot suspend admins (unless super admin)
        if ((user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN)
                && admin.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only Super Admin can suspend admins");
        }

        // Suspend
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        // Log action
        auditService.logAction(
                adminId,
                admin.getEmail(),
                "SUSPEND_USER",
                userId,
                user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                dto.getReason(),
                null,
                null
        );

        log.info("User suspended: {}", userId);

        return new MessageResponseDto(
                "User suspended successfully",
                true
        );
    }

    /**
     * Ban user
     */
    @Transactional
    public MessageResponseDto banUser(UUID adminId, UUID userId, UserActionDto dto) {
        log.info("Admin {} banning user {}", adminId, userId);

        // Get admin
        User admin = getAdminUser(adminId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Cannot ban admins (unless super admin)
        if ((user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN)
                && admin.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only Super Admin can ban admins");
        }

        // Ban
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);

        // Log action
        auditService.logAction(
                adminId,
                admin.getEmail(),
                "BAN_USER",
                userId,
                user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                dto.getReason(),
                null,
                null
        );

        log.info("User banned: {}", userId);

        return new MessageResponseDto(
                "User banned successfully",
                true
        );
    }

    /**
     * Reactivate user
     */
    @Transactional
    public MessageResponseDto reactivateUser(UUID adminId, UUID userId, UserActionDto dto) {
        log.info("Admin {} reactivating user {}", adminId, userId);

        // Get admin
        User admin = getAdminUser(adminId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Reactivate
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Log action
        auditService.logAction(
                adminId,
                admin.getEmail(),
                "REACTIVATE_USER",
                userId,
                user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
                dto.getReason(),
                null,
                null
        );

        log.info("User reactivated: {}", userId);

        return new MessageResponseDto(
                "User reactivated successfully",
                true
        );
    }

    // ============ DASHBOARD & STATS ============

    /**
     * Get dashboard statistics
     */
    public DashboardStatsDto getDashboardStats() {
        log.info("Fetching dashboard statistics");

        LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime weekStart = today.minusDays(7);
        LocalDateTime monthStart = today.minusDays(30);

        return DashboardStatsDto.builder()
                // User counts
                .totalUsers(userRepository.count())
                .totalTraders(userRepository.countByRole(Role.TRADER))
                .totalAgents(userRepository.countByRole(Role.AGENT))
                .totalBuyers(userRepository.countByRole(Role.BUYER))
                .totalAdmins(userRepository.countByRole(Role.ADMIN) + userRepository.countByRole(Role.SUPER_ADMIN))

                // Agent stats
                .pendingAgents(agentRepository.countByApprovalStatus(ApprovalStatus.PENDING))
                .approvedAgents(agentRepository.countByApprovalStatus(ApprovalStatus.APPROVED))
                .rejectedAgents(agentRepository.countByApprovalStatus(ApprovalStatus.REJECTED))

                // User status
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .suspendedUsers(userRepository.countByStatus(UserStatus.SUSPENDED))
                .bannedUsers(userRepository.countByStatus(UserStatus.BANNED))

                // Time-based
                .todayRegistrations(userRepository.countByCreatedAtAfter(today))
                .thisWeekRegistrations(userRepository.countByCreatedAtAfter(weekStart))
                .thisMonthRegistrations(userRepository.countByCreatedAtAfter(monthStart))
                .build();
    }

    /**
     * Get audit logs
     */
    public Page<AdminAuditLog> getAuditLogs(Pageable pageable) {
        log.info("Fetching audit logs");
        return auditService.getAllLogs(pageable);
    }

    // ============ HELPER METHODS ============

    private User getAdminUser(UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("User is not an admin");
        }

        return admin;
    }
    private AgentDetailDto mapToAgentDetailDto(Agent agent) {
        // Use MapStruct for the basic mapping
        AgentDetailDto dto = agentMapper.toDetailResponse(agent);

        // Manually set the fields MapStruct can't handle
        User user = agent.getUser();

        // Count traders registered by this agent
        Integer tradersCount = traderRepository.findByRegisteredByAgentId(user.getId()).size();
        dto.setTradersRegistered(tradersCount);

        // Get approver email if approved
        if (agent.getApprovedBy() != null) {
            String approverEmail = userRepository.findById(agent.getApprovedBy())
                    .map(User::getEmail)
                    .orElse(null);
            dto.setApprovedByAdminEmail(approverEmail);
        }

        return dto;
    }
}