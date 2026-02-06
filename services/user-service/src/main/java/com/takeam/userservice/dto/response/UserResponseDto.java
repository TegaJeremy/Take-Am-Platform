package com.takeam.userservice.dto.response;


import com.takeam.userservice.model.Role;
import com.takeam.userservice.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String phoneNumber;
    private String fullName;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
