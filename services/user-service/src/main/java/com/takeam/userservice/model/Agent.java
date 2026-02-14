package com.takeam.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "assigned_market_id", nullable = false)
    private String assignedMarketId;

    @Column(name = "identity_type")
    private String identityType;

    @Column(name = "identity_document")
    private String identityDocument;

    @Column(name = "biometric_template", columnDefinition = "TEXT")
    private String biometricTemplate;

    @Column(name = "face_photo_url")
    private String facePhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;  // Which admin approved

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}