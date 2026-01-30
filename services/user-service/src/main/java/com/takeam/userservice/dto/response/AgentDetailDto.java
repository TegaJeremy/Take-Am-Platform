package com.takeam.userservice.dto.response;

import com.takeam.userservice.models.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDetailDto {
    private UserResponseDto user;
    private String assignedMarketId;
    private String identityType;
    private String identityDocument;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private String approvedByAdminEmail;
    private Integer tradersRegistered;
}