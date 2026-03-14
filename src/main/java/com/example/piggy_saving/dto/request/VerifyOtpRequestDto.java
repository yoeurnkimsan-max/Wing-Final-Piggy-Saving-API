package com.example.piggy_saving.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class VerifyOtpRequestDto {

    @NotBlank
    private UUID userId;

    @NotBlank
    private String otpCode;
}
