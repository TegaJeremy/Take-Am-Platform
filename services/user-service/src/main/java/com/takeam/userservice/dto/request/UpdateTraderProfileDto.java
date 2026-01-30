package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTraderProfileDto {

    @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
    private String fullName;

    private String stallNumber;

    @Size(min = 10, max = 10, message = "Bank account number must be 10 digits")
    private String bankAccountNumber;

    @Size(min = 3, max = 3, message = "Bank code must be 3 characters")
    private String bankCode;

    private String bankName;

    private String accountName;
}