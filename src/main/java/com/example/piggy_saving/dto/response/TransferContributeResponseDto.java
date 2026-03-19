package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.GoalStatus;
import com.example.piggy_saving.models.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    private UUID transactionId;       // e.g., "tx-003"
    private UUID senderAccountId;     // clearer than fromAccountId
    private UUID recipientAccountId;  // clearer than toAccountId
    private BigDecimal amount;          // contribution amount
    private TransactionType transactionType;     // e.g., "contribution"
    private String goalName;            // e.g., "Birthday Fund"
    private String goalOwner;           // e.g., "Jane Smith"
    private String description;         // contribution description
    private BigDecimal newMainBalance;  // updated main account balance
    private GoalStatus status;    // easier boolean naming

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime completedAt;  // timestamp of completion
}