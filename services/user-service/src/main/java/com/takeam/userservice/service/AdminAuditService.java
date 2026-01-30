package com.takeam.userservice.service;

import com.takeam.userservice.models.AdminAuditLog;
import com.takeam.userservice.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuditService {

    private final AdminAuditLogRepository auditLogRepository;

    /**
     * Log admin action
     */
    public void logAction(
            UUID adminId,
            String adminEmail,
            String action,
            UUID targetUserId,
            String targetUserEmail,
            String reason,
            String notes,
            String ipAddress) {

        AdminAuditLog auditLog = new AdminAuditLog();
        auditLog.setAdminId(adminId);
        auditLog.setAdminEmail(adminEmail);
        auditLog.setAction(action);
        auditLog.setTargetUserId(targetUserId);
        auditLog.setTargetUserEmail(targetUserEmail);
        auditLog.setReason(reason);
        auditLog.setNotes(notes);
        auditLog.setIpAddress(ipAddress);

        auditLogRepository.save(auditLog);

        log.info("Admin action logged: {} by {} on {}", action, adminEmail, targetUserEmail);
    }

    /**
     * Get all audit logs (paginated)
     */
    public Page<AdminAuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Get logs by admin
     */
    public Page<AdminAuditLog> getLogsByAdmin(UUID adminId, Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get logs for specific user
     */
    public Page<AdminAuditLog> getLogsByTargetUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}