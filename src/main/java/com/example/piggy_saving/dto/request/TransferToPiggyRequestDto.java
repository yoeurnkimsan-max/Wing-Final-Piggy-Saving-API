package com.example.piggy_saving.dto.request;

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
public class TransferToPiggyRequestDto {

    @NotNull(message = "Piggy account ID must not be null")
    private String accountPiggyNumber;

    @NotNull(message = "Transfer Amount must not be null")
    @Positive(message = "Transfer Amount must be greater than zero")
    private BigDecimal transferAmount;


}