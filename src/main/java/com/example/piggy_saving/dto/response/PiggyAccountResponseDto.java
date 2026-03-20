package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.GoalStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PiggyAccountResponseDto {

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("is_public")
    private boolean isPublic;

    // Piggy goal info
    @JsonProperty("piggy_goal_id")
    private UUID piggyGoalId;

    @JsonProperty("goal_name")
    private String goalName;

    @JsonProperty("goal_status")
    private GoalStatus goalStatus;

    @JsonProperty("current_balance")
    private BigDecimal currentBalance;

    @JsonProperty("target_amount")
    private BigDecimal targetAmount;

    @JsonProperty("locked_at")
    private LocalDateTime lockedAt;

    @JsonProperty("lock_expires_at")
    private LocalDateTime lockExpiresAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}