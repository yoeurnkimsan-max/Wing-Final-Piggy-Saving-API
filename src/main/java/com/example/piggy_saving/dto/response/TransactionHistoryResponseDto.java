package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.EntryType;
import com.example.piggy_saving.models.enums.TransactionStatus;
import com.example.piggy_saving.models.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class TransactionHistoryResponseDto {

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("transaction_type")
    private TransactionType transactionType;

    @JsonProperty("status")
    private TransactionStatus status;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("counterparty_name")
    private String counterpartyName;

    @JsonProperty("counterparty_email")
    private String counterpartyEmail;

    @JsonProperty("goal_name")
    private String goalName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("from_account_mask")
    private String fromAccountMask;

    @JsonProperty("to_account_mask")
    private String toAccountMask;

    @JsonProperty("entry_type")
    private EntryType entryType; // DEBIT or CREDIT from user's perspective

    @JsonProperty("balance_after")
    private BigDecimal balanceAfter;

    private Map<String, Object> metadata;
}