package com.takeam.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private UUID id;
    private UUID agentId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime clockInTime;

    private Double clockInLatitude;
    private Double clockInLongitude;
    private String clockInAddress;
    private Boolean isInMarketZone;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime clockOutTime;

    private Double clockOutLatitude;
    private Double clockOutLongitude;
    private String clockOutAddress;
    private Double totalHoursWorked;
    private String status;
    private Integer completedPickups;

    private String message;
}