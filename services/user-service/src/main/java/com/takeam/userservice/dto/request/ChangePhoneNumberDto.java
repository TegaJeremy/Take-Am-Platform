package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePhoneNumberDto {

    @NotBlank(message = "New phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String newPhoneNumber;

    @NotBlank(message = "OTP is required")
    private String otp;
}