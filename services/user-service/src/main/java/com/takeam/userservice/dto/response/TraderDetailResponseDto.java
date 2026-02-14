package com.takeam.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraderDetailResponseDto {
    private UserResponseDto user;
    private String marketId;
    private String stallNumber;
    private String bankAccountNumber;
    private String bankCode;
    private String bankName;
    private String accountName;
    private Boolean verified;
    private LocalDateTime createdAt;
}