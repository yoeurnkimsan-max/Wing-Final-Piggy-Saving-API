package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.models.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferP2PResponseDto {

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("from_account_id")
    private UUID fromAccountId;

    @JsonProperty("to_account_id")
    private UUID toAccountId;

    private BigDecimal amount;

    private TransferType type;

    @JsonProperty("recipient_name")
    private String recipientName;

    private String description;

    @JsonProperty("new_main_balance")
    private BigDecimal newMainBalance;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
}