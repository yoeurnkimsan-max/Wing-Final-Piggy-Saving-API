package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.LoginRequestDto;
import com.example.piggy_saving.dto.request.RegisterRequestDto;
import com.example.piggy_saving.dto.request.VerifyOtpRequestDto;
import com.example.piggy_saving.dto.response.LoginResponseDto;
import com.example.piggy_saving.dto.response.RegisterResponseDto;
import com.example.piggy_saving.services.AuthService;
import com.example.piggy_saving.services.EmailOtpService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(AuthController.BASE_ROUTE)
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailOtpService emailOtpService;
    public static final String BASE_ROUTE = "/api/v1/auth"; // made public for reuse

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email, @RequestParam(required = false) String userName) {
        try {
            // Use email as name if not provided
            String displayName = (userName != null && !userName.isEmpty()) ? userName : email.split("@")[0];

            boolean sent = emailOtpService.sendOtp(email, displayName);

            if (sent) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "OTP sent successfully to " + email
                ));
            } else {
                return ResponseEntity.internalServerError().body(Map.of(
                        "success", false,
                        "message", "Failed to send OTP. Please try again."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("verify-otp")
    public ResponseEntity<LoginResponseDto> otpVerify(@Valid @RequestBody VerifyOtpRequestDto requestDto){
        LoginResponseDto response = authService.verifyOtp(requestDto.getEmail(), requestDto.getOtpCode());
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
