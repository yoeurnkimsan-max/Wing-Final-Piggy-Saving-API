package com.example.piggy_saving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TransferBreakRequestDto {

    @NotNull(message = "Piggy goal ID is required")
    @JsonProperty("piggy_account_number")
    private String piggyAccountNumber;
}
