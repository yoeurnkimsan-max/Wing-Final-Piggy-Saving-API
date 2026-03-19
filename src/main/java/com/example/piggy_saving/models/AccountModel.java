package com.example.piggy_saving.models;

import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.util.AccountNumberGenerator;
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
    private UserModel userModel; // required for main accounts, optional for piggy accounts

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;


    @Column(name = "is_public")
    private boolean isPublic ; // default: private

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piggy_goal_id", unique = true)
    private PiggyGoalModel piggyGoalModel; // optional, only for PIGGY accounts

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

    /**
     * Combined PrePersist / PreUpdate listener
     * - Validates account
     * - Generates account number if null
     */
    @PrePersist
    @PreUpdate
    private void beforeSave() {
        // --- Validation ---
        if (accountType == AccountType.MAIN && userModel == null) {
            throw new IllegalStateException("Main account must be linked to a user");
        }
        if (accountType == AccountType.PIGGY && piggyGoalModel == null) {
            throw new IllegalStateException("Piggy account must be linked to a PiggyGoal");
        }

        // --- Generate account number if null ---
        if (this.accountNumber == null) {
            this.accountNumber = AccountNumberGenerator.generateAccountNumber(1); // branch code
        }

    }
}