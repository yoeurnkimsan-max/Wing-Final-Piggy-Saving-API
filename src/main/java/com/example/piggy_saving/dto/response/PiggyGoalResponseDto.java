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
public class PiggyGoalResponseDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("lock_period_days")
    private int lockPeriodDays;

    @JsonProperty("hide_balance")
    private boolean hideBalance;

    @JsonProperty("status")
    private GoalStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("lock_expires_at")
    private LocalDateTime lockExpiresAt;

    @JsonProperty("locked_at")
    private LocalDateTime lockedAt;

    @JsonProperty("broken_at")
    private LocalDateTime brokenAt;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}