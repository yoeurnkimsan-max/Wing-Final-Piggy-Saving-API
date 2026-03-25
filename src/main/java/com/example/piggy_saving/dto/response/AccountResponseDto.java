package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.dto.response.statusEnum.AccountStatus;
import com.example.piggy_saving.models.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AccountResponseDto {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("username")
    private String username;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("account_type")
    private AccountType accountType;

    @JsonProperty("current_balance")
    private BigDecimal balance;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("is_public")
    private boolean isPublic;

    @JsonProperty("piggy_goal_id")
    private UUID piggyGoalId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
