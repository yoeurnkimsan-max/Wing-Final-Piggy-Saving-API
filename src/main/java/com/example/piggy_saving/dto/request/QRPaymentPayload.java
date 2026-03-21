package com.example.piggy_saving.dto.request;

import com.example.piggy_saving.models.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QRPaymentPayload {

    @JsonProperty("type")
    private TransferType type; // "OWN_TRANSFER", "P2P", "CONTRIBUTION"

    // For OWN_TRANSFER
    @JsonProperty("account_piggy_number")
    private String accountPiggyNumber;

    // For P2P
    @JsonProperty("recipient_account_number")
    private String recipientAccountNumber;

    // For CONTRIBUTION
    @JsonProperty("piggy_account_number")
    private String piggyAccountNumber;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("version")
    private String version;
}