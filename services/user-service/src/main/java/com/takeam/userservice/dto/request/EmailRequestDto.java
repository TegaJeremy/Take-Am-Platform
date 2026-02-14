package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}