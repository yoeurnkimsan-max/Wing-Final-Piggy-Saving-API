package com.example.piggy_saving.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferToPiggyRequestDto {

    @NotNull(message = "Piggy account ID must not be null")
    private String accountPiggyNumber;

    @NotNull(message = "Transfer Amount must not be null")
    @Positive(message = "Transfer Amount must be greater than zero")
    private BigDecimal transferAmount;


}