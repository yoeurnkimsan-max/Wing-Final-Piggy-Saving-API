package com.example.piggy_saving.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class VerifyOtpRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otpCode;
}
