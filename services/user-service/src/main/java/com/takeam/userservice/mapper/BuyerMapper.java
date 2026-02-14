package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.request.BuyerRegistrationDto;
import com.takeam.userservice.model.Buyer;
import com.takeam.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BuyerMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "BUYER")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "loginAttempts", constant = "0")
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(BuyerRegistrationDto dto);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaultShippingAddress", ignore = true)
    @Mapping(target = "postalCode", ignore = true)
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Buyer toBuyer(BuyerRegistrationDto dto);
}