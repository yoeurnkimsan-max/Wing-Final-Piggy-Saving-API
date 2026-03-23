package com.example.piggy_saving.dto.request;

import com.example.piggy_saving.models.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRPaymentPayload {

    @JsonProperty("type")
    private TransferType type; // "OWN_TRANSFER", "P2P", "CONTRIBUTION"

    // For P2P
    @JsonProperty("recipient_account_number")
    private String recipientAccountNumber;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("version")
    private String version;
}