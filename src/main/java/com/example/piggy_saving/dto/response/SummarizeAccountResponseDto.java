package com.example.piggy_saving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SummarizeAccountResponseDto
{
    @JsonProperty("total_save")
    private BigDecimal totalSave;
    @JsonProperty("monthly_spent")
    private BigDecimal monthlySpent;
    @JsonProperty("active_goal")
    private int activeGoal;
}
