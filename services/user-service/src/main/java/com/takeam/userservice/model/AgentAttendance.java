package com.takeam.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_attendance", indexes = {
        @Index(name = "idx_agent_date", columnList = "agentId, date"),
        @Index(name = "idx_date", columnList = "date"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

//    @Column(nullable = false, columnDefinition = "UUID")
    @Column(nullable = false, name = "agent_id")
    private UUID agentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalDateTime clockInTime;

    @Column(nullable = false)
    private Double clockInLatitude;

    @Column(nullable = false)
    private Double clockInLongitude;

    private String clockInAddress;

    @Column(nullable = false)
    private Boolean isInMarketZone;

    private LocalDateTime clockOutTime;

    private Double clockOutLatitude;

    private Double clockOutLongitude;

    private String clockOutAddress;

    private Double totalHoursWorked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private Integer completedPickups;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (date == null) {
            date = LocalDate.now();
        }
        if (status == null) {
            status = AttendanceStatus.CLOCKED_IN;
        }
        if (isInMarketZone == null) {
            isInMarketZone = false;
        }
        if (completedPickups == null) {
            completedPickups = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AttendanceStatus {
        CLOCKED_IN,
        CLOCKED_OUT
    }
}