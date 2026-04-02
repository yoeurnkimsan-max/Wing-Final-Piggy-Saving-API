package com.example.piggy_saving.models;

import com.example.piggy_saving.models.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "piggy_goals",
        indexes = @Index(name = "idx_piggy_goals_user", columnList = "user_id"))
public class PiggyGoalModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel userModel;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "target_amount", nullable = true, precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(name = "lock_period_days", nullable = false)
    private Integer lockPeriodDays;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "lock_expires_at", nullable = false)
    private LocalDateTime lockExpiresAt;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "broken_at", nullable = true)
    private LocalDateTime brokenAt;

    @Column(name = "completed_at", nullable = true)
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Builder.Default
    @Column(name = "accrued_interest", nullable = false, precision = 19, scale = 4)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal interestRate = BigDecimal.valueOf(0.03); // default 3% annual

    @Column(name = "last_interest_calculated_at")
    private LocalDateTime lastInterestCalculatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional mapping to account
    @OneToOne(mappedBy = "piggyGoalModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AccountModel accountModel;

    @OneToMany(mappedBy = "piggyGoalModel")
    private List<InterestPaymentModel> interestPaymentModels;
}
