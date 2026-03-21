package com.example.piggy_saving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TransferP2PRequestDto {
    @NotNull(message = "Piggy account ID must not be null")
    @JsonProperty("recipient_account_number")
    private String recipientAccountNumber;

    @NotNull(message = "Transfer Amount must not be null")
    @Positive(message = "Transfer Amount must be greater than zero")
    @JsonProperty("amount")
    private BigDecimal amount;

}
