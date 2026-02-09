package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnifiedLoginDto {

    @NotBlank(message = "Identifier is required (phone or email)")
    private String identifier;

    private String password;


}