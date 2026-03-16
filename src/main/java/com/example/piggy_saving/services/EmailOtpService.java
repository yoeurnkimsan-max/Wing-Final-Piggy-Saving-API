package com.example.piggy_saving.services;

import com.example.piggy_saving.exception.UserNotFoundException;
import com.example.piggy_saving.models.OtpVerificationModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.repository.OtpVerificationRepository;
import com.example.piggy_saving.repository.UserRepository;
import com.example.piggy_saving.security.PasswordEncoderConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private static final Logger logger = LoggerFactory.getLogger(EmailOtpService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String OTP_CACHE = "otpCache"; // should match your cache config
    private final OtpVerificationRepository otpVerificationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoderConfig passwordEncoderConfig;

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private CacheManager cacheManager;

    // Fallback in-memory store if cache not configured
    private final Map<String, OtpData> fallbackStore = new ConcurrentHashMap<>();

    /**
     * Generate a 6-digit OTP
     */
    private String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send OTP to email and store it with expiration
     */
    public boolean sendOtp(String email, String userName) {
        try {
            String otpCode = generateOtp();

            UserModel userModel = userRepository.findUserModelByEmail(email);
            if (userModel == null) {
                throw new UserNotFoundException("User not found with email: " + email);
            }

            OtpVerificationModel otpVerificationModel = OtpVerificationModel.builder()
                    .optCode(passwordEncoderConfig.passwordEncoder().encode(otpCode))
                    .userModel(userModel)
                    .attempts(3)
                    .email(email)
                    .verified(false)
                    .expiresAt(java.time.LocalDateTime.now().plusMinutes(5))
                    .build();
            otpVerificationRepository.save(otpVerificationModel);

            storeOtp(email, otpCode);

            CompletableFuture<Boolean> emailFuture = emailService.sendOtpEmail(email, otpCode, userName);
            Boolean emailSent = emailFuture.get();

            if (emailSent) {
                logger.info("OTP sent and stored for email: {}", email);
                return true;
            } else {
                removeOtp(email);
                logger.warn("Failed to send OTP email for: {}", email);
                return false;
            }

        } catch (UserNotFoundException e) {
            // Re-throw to let controller handle it as 404
            throw e;
        } catch (Exception e) {
            logger.error("Error sending OTP for email: {}", email, e);
            return false;
        }
    }

    /**
     * Store OTP with 5-minute expiration
     */
    private void storeOtp(String email, String otpCode) {
        long expiryTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes

        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(OTP_CACHE);
            if (cache != null) {
                cache.put(email, new OtpData(otpCode, expiryTime));
                return;
            }
        }

        // Fallback in-memory store
        fallbackStore.put(email, new OtpData(otpCode, expiryTime));
    }

    /**
     * Verify OTP entered by user
     */
    public boolean verifyOtp(String email, String otpCode) {

        UserModel user = userRepository.findUserModelByEmail(email);

        if (user == null) {
            logger.warn("User not found for email: {}", email);
            return false;
        }

        OtpVerificationModel otp = otpVerificationRepository
                .findTopByUserModelOrderByCreatedAtDesc(user);

        if (otp == null) {
            logger.warn("No OTP found for email: {}", email);
            return false;
        }

        if (otp.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            logger.warn("OTP expired for email: {}", email);
            return false;
        }

        boolean isValid = passwordEncoderConfig.passwordEncoder()
                .matches(otpCode, otp.getOptCode());

        if (!isValid) {
            logger.warn("Invalid OTP attempt for email: {}", email);
            return false;
        }

        otp.setVerified(true);
        otpVerificationRepository.save(otp);

        logger.info("OTP verified successfully for email: {}", email);

        return true;
    }

    /**
     * Retrieve stored OTP
     */
    private OtpData getStoredOtp(String email) {
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(OTP_CACHE);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(email);
                if (wrapper != null) {
                    return (OtpData) wrapper.get();
                }
            }
        }
        return fallbackStore.get(email);
    }

    /**
     * Remove OTP
     */
    private void removeOtp(String email) {
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(OTP_CACHE);
            if (cache != null) {
                cache.evict(email);
                return;
            }
        }
        fallbackStore.remove(email);
    }

    /**
     * Inner class to hold OTP and expiry
     */
    private static class OtpData {
        final String otpCode;
        final long expiryTime;

        OtpData(String otpCode, long expiryTime) {
            this.otpCode = otpCode;
            this.expiryTime = expiryTime;
        }
    }
}
