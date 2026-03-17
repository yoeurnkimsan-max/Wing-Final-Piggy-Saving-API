package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.TransactionType;
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
    private TransactionType type;

    @JsonProperty("description")
    private String description;

}