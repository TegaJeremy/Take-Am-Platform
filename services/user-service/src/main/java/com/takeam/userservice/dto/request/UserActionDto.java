package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserActionDto {

    @NotBlank(message = "Reason is required")
    private String reason;
}