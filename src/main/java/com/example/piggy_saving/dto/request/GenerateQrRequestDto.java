package com.example.piggy_saving.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GenerateQrRequestDto {
    private BigDecimal amount;   // optional (can be null)
    private String currency;     // default USD
    private String note;         // optional message

}
