package com.takeam.userservice.model;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "traders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trader {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name="user_id", nullable=false, unique = true)
    private User user;

//    @Column(name = "market_id", nullable = false)
//    private String marketId;
//
//    @Column(name = "stall_number")
//    private String stallNumber;

    @Column(name = "bank_account_number", nullable = false, length = 10)
    private String bankAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_name")
    private String accountName;

    @Column(nullable = false)
    private Boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "registered_by_agent_id")
    private UUID registeredByAgentId;


}
