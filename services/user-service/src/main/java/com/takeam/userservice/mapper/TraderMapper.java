package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.request.TraderDetailsDTO;
import com.takeam.userservice.dto.request.TraderRegistrationRequestDto;
import com.takeam.userservice.dto.response.TraderDetailResponseDto;
import com.takeam.userservice.model.Trader;
import com.takeam.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraderMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "TRADER")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "loginAttempts", constant = "0")
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(TraderRegistrationRequestDto dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    Trader toTrader(TraderRegistrationRequestDto dto);

    @Mapping(source = "user", target = "user")
    TraderDetailResponseDto toDetailResponse(Trader trader);


}