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
public class LoginResponseDto {

    private String status;

    private LoginData data;

    private String message;




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginData {

        @JsonProperty("user_id")
        private UUID userId;

        private String email;

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("token_type")
        private String tokenType = "Bearer";

        @JsonProperty("role")
        private List<String> roles;

        @JsonProperty("access_token_expires_in")
        private int accessTokenExpiresIn;
    }
}
