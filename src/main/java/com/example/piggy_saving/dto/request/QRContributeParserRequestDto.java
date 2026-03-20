package com.example.piggy_saving.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QRContributeParserRequestDto {
    private String qrText;
}
