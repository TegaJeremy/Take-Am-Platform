package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.request.AgentRegistrationRequestDto;
import com.takeam.userservice.dto.response.AgentDetailDto;
//import com.takeam.userservice.dto.response.AgentStatusResponse;
import com.takeam.userservice.model.Agent;
import com.takeam.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "AGENT")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "loginAttempts", constant = "0")
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(AgentRegistrationRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "approvalStatus", constant = "PENDING")
    @Mapping(target = "biometricTemplate", ignore = true)
    @Mapping(target = "facePhotoUrl", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Agent toAgent(AgentRegistrationRequestDto dto);


    @Mapping(target = "user", source = "user")
    @Mapping(target = "tradersRegistered", ignore = true)
    @Mapping(target = "approvedByAdminEmail", ignore = true)
    @Mapping(source = "id", target = "agentId")
    AgentDetailDto toDetailResponse(Agent agent);


//    @Mapping(target = "agentId", source = "id")
//    @Mapping(target = "isActive", expression = "java(agent.getStatus() == AgentStatus.ACTIVE)")
//    AgentStatusResponse toStatusResponse(Agent agent);
}