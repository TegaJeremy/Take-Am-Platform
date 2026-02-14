package com.takeam.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraderDetailsDTO {
    private UUID id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String businessName;
    private String businessAddress;
    private boolean verified;
    private boolean active;
}