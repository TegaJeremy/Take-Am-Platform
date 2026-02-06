package com.takeam.userservice.repository;

import com.takeam.userservice.model.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {

    Page<AdminAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<AdminAuditLog> findByAdminIdOrderByCreatedAtDesc(UUID adminId);

    List<AdminAuditLog> findByTargetUserIdOrderByCreatedAtDesc(UUID targetUserId);
}