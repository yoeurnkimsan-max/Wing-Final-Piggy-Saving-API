package com.example.piggy_saving.controllers;

import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.services.EmailOtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/email-otp")
public class EmailOtpController {

    private static final Logger logger = LoggerFactory.getLogger(EmailOtpController.class);

    @Autowired
    private EmailOtpService emailOtpService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestParam String email,
                                     @RequestParam(required = false) String userName) {
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
            logger.error("Error in send OTP endpoint", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email,
                                       @RequestParam String otpCode) {
        try {
            boolean isValid = emailOtpService.verifyOtp(email, otpCode);

            if (isValid) {


                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "OTP verified successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid or expired OTP"
                ));
            }
        } catch (Exception e) {
            logger.error("Error in verify OTP endpoint", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }
}