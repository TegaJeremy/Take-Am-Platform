package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OTPVerificationDto {


    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;


    @Deprecated
    public String getPhoneNumber() {
        return identifier;
    }

    @Deprecated
    public void setPhoneNumber(String phoneNumber) {
        this.identifier = phoneNumber;
    }

    public String getEmail() {
        return identifier;
    }

    public void setEmail(String email) {
        this.identifier = email;
    }
}
