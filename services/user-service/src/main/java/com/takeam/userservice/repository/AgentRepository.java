package com.takeam.userservice.repository;

import com.takeam.userservice.model.Agent;
import com.takeam.userservice.model.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends JpaRepository<Agent, UUID> {

    Optional<Agent> findByUserId(UUID userId);

    List<Agent> findByApprovalStatus(ApprovalStatus status);

    Page<Agent> findByApprovalStatus(ApprovalStatus status, Pageable pageable);

    List<Agent> findByAssignedMarketId(String marketId);

    List<Agent> findByAssignedMarketIdAndApprovalStatus(String marketId, ApprovalStatus status);

    Long countByApprovalStatus(ApprovalStatus status);
}