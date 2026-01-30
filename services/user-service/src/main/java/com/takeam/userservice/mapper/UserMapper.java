package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.models.User;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
}
