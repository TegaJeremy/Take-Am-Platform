package com.takeam.userservice.dto.response;

import com.takeam.userservice.models.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDetailResponseDto {
    private UserResponseDto user;
    private String assignedMarketId;
    private String identityType;
    private String identityDocument;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime approvedAt;
//    private List<ProxyRegistrationDto> proxyRegistrations; // Traders registered by agent
}