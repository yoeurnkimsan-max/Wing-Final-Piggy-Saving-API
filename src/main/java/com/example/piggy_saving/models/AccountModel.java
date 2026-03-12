package com.example.piggy_saving.models;

import com.example.piggy_saving.models.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "accounts",
        indexes = {
                @Index(name = "idx_accounts_user", columnList = "user_id"),
                @Index(name = "idx_accounts_piggy_goal", columnList = "piggy_goal_id")
        })
public class AccountModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserModel userModel; // null for piggy accounts

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piggy_goal_id", unique = true)
    private PiggyGoalModel piggyGoalModel; // null for main accounts

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Business rule: exactly one of user or piggyGoal must be non‑null.
    // This can be enforced with @PrePersist/@PreUpdate or database trigger.
    @PrePersist
    @PreUpdate
    private void validate() {
        if ((userModel == null) == (piggyGoalModel == null)) {
            throw new IllegalStateException("Exactly one of user or piggyGoal must be set");
        }
    }
}
