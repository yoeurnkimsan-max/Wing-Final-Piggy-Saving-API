package com.example.piggy_saving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDto {

    private String status;

    private RegisterData data;

    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterData {

        @JsonProperty("user_id")
        private UUID userId;

        @JsonProperty("email")
        private String email;

        @JsonProperty("OTP_expires_in")
        private int otpExpiresIn;
    }
}