package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
}
