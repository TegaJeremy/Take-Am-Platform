package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TraderRegistrationRequestDto {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
    private String fullName;

//    @NotBlank(message = "Market ID is required")
//    private String marketId;

    private String stallNumber;

    @NotBlank(message = "Bank account number is required")
    @Size(min = 10, max = 10, message = "Bank account number must be 10 digits")
    private String bankAccountNumber;

//    @NotBlank(message = "Bank code is required")
//    @Size(min = 3, max = 3, message = "Bank code must be 3 characters")
//    private String bankCode;

    private String bankName;

    private String accountName;
}