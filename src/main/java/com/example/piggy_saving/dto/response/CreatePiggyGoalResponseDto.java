package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@Builder
public class CreatePiggyGoalResponseDto {
    private PiggyGoalResponseDto piggyGoal;
    private AccountResponseDto account;
}
