package com.example.piggy_saving.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QRPaymentRequest {
    private String qrData;
    private BigDecimal amount;
    private String notes;
}
