package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.response.UserLookupResponseDto;
import com.takeam.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserLookupMapper {

      @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserLookupResponseDto toUserLookupResponse(User user);
}