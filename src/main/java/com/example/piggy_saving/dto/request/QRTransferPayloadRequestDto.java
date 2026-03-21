package com.example.piggy_saving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRTransferPayloadRequestDto {
    @JsonProperty("qr_base64_data")
    String qrBase64String;

    BigDecimal amount;
}
