package com.takeam.userservice.mapper;

import com.takeam.userservice.dto.response.UserLookupResponseDto;
import com.takeam.userservice.dto.response.UserResponseDto;
import com.takeam.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);

}
