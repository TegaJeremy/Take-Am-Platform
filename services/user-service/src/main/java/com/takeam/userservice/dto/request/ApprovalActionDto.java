package com.takeam.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalActionDto {

    @NotBlank(message = "Reason is required")
    private String reason;

    private String notes;
}