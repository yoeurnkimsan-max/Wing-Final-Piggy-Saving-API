package com.example.piggy_saving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DecodeQRRequest {
    @JsonProperty("qr_data")
    private String qrData;
}
