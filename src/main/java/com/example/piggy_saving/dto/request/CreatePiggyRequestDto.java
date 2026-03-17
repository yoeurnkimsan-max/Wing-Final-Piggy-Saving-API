package com.example.piggy_saving.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePiggyRequestDto {

    @NotBlank(message = "Goal name is required")
    private String name;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    private boolean hideBalance;

    @NotNull(message = "Lock period in days is required")
    @Min(value = 1, message = "Lock period must be at least 1 day")
    private Integer lockPeriodDays;
}