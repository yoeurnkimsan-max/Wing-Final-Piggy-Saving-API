package com.example.piggy_saving.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
public class GenerateQrResponseDto {
    private String qrData;       // encoded string
    private String reference;    // unique transaction ref
    private BigDecimal amount;
    private String currency;

}
