package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.GoalStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PiggyGoalDetailResponseDto {

    // Goal fields
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("target_amount")
    private BigDecimal targetAmount;

    @JsonProperty("status")
    private GoalStatus status;

    @JsonProperty("lock_expires_at")
    private LocalDateTime lockExpiresAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Account fields
    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("current_balance")
    private BigDecimal currentBalance;

    @JsonProperty("is_public")
    private boolean isPublic;

    @JsonProperty("hide_balance")
    private boolean hideBalance;

    @JsonProperty("currency")
    private String currency;
}