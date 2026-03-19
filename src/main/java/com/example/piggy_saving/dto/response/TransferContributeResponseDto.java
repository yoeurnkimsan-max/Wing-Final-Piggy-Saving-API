package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.GoalStatus;
import com.example.piggy_saving.models.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferContributeResponseDto {

    @JsonProperty("transaction_id")
    private UUID transactionId;       // e.g., "tx-003"
    @JsonProperty("sender_account_id")
    private UUID senderAccountId;     // clearer than fromAccountId
    @JsonProperty("sender_account_number")
    private String senderAccountNumber;
    @JsonProperty("recipient_account_id")
    private UUID recipientAccountId;  // clearer than toAccountId
    @JsonProperty("recipient_account_number")
    private String recipientAccountNumber;
    @JsonProperty("amount")
    private BigDecimal amount;          // contribution amount
    @JsonProperty("transaction_type")
    private TransactionType transactionType;     // e.g., "contribution"
    @JsonProperty("goal_name")
    private String goalName;            // e.g., "Birthday Fund"
    @JsonProperty("goal_owner")
    private String goalOwner;           // e.g., "Jane Smith"
    @JsonProperty("description")
    private String description;         // contribution description
    @JsonProperty("current_main_balance")
    private BigDecimal newMainBalance;  // updated main account balance
    @JsonProperty("status")
    private GoalStatus status;    // easier boolean naming

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime completedAt;  // timestamp of completion
}