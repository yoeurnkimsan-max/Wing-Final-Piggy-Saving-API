package com.example.piggy_saving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class QRProcessResponseDto {
    @JsonProperty("type")
    private String type; // "P2P", "CONTRIBUTION", "OWN_TRANSFER"

    @JsonProperty("valid")
    private boolean valid;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("recipient")
    private Map<String, Object> recipient; // For P2P transfers

    @JsonProperty("goal")
    private Map<String, Object> goal; // For CONTRIBUTION and OWN_TRANSFER

    @JsonProperty("transaction_details")
    private Map<String, Object> transactionDetails; // Min/max amounts, currency

    @JsonProperty("error")
    private String error; // Optional error message

    @JsonProperty("error_code")
    private String errorCode; // "EXPIRED", "INVALID", "NOT_FOUND"
}
