package com.example.piggy_saving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    @NotNull(message = "Recipient account number ID is required")
    @JsonProperty("recipient_account_number")
    private String recipientAccountNumber;   // maps to "piggy_goal_id"

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;    // contribution amount


    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String notes;
}
