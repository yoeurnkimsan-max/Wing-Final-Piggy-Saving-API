package com.example.piggy_saving.dto.request;

import com.example.piggy_saving.models.enums.TransferType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QRValidationResponse {
    private TransferType type;
    private String recipientAccountNumber;
    private String expiresAt;
}