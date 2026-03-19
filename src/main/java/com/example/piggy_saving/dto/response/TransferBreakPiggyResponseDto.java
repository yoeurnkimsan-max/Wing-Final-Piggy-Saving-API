package com.example.piggy_saving.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransferBreakPiggyResponseDto {
    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("piggy_goal_id")
    private UUID piggyGoalId;

    @JsonProperty("goal_name")
    private String goalName;

    @JsonProperty("original_balance")
    private BigDecimal originalBalance;

    @JsonProperty("penalty_percentage")
    private BigDecimal penaltyPercentage;

    @JsonProperty("penalty_amount")
    private BigDecimal penaltyAmount;

    @JsonProperty("return_amount")
    private BigDecimal returnAmount;

    @JsonProperty("new_main_balance")
    private BigDecimal newMainBalance;

    @JsonProperty("was_early_break")
    private Boolean wasEarlyBreak;

    @JsonProperty("completed_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime completedAt;
}
