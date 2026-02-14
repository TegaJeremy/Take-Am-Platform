package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.response.AttendanceResponse;
import com.takeam.userservice.model.AgentAttendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "message", ignore = true)
    AttendanceResponse toResponse(AgentAttendance attendance);

    @Named("statusToString")
    default String statusToString(AgentAttendance.AttendanceStatus status) {
        return status != null ? status.name() : null;
    }
}