package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.models.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransferResponseDto {

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("from_account_id")
    private UUID fromAccountId;

    @JsonProperty("to_account_id")
    private UUID toAccountId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("type")
    private TransferType type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("new_main_balance")
    private BigDecimal newMainBalance;

    @JsonProperty("new_piggy_balance")
    private BigDecimal newPiggyBalance;

    @JsonProperty("goal_completed")
    private Boolean goalCompleted;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}
