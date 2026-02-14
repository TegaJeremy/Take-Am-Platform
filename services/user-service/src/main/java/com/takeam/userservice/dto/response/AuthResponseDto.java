package com.takeam.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String message;
    private String identifier;
    private boolean otpSent;
    private String otp;  // OTP included in response for frontend testing

    // Constructor without OTP (for backward compatibility)
    public AuthResponseDto(String message, String identifier, Boolean requiresOTP) {
        this.message = message;
        this.identifier = identifier;
        this.otpSent = requiresOTP;
        this.otp = null;
    }
}