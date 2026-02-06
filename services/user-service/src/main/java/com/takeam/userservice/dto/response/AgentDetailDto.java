package com.takeam.userservice.dto.response;

import com.takeam.userservice.model.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDetailDto {
    private UserResponseDto user;
    private UUID agentId;
    private String assignedMarketId;
    private String identityType;
    private String identityDocument;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private String approvedByAdminEmail;
    private Integer tradersRegistered;
}