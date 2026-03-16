package com.example.piggy_saving.services;

import com.example.piggy_saving.repository.OtpVerificationRepository;
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

            // Store OTP with expiration
            storeOtp(email, otpCode);

            // Send email asynchronously
            CompletableFuture<Boolean> emailFuture = emailService.sendOtpEmail(email, otpCode, userName);

            // Wait for email result (can also make controller async)
            Boolean emailSent = emailFuture.get();

            if (emailSent) {

//                otpVerificationRepository.

                logger.info("OTP sent and stored for email: {}", email);
                return true;
            } else {
                // Remove stored OTP if email failed
                removeOtp(email);
                logger.warn("Failed to send OTP email for: {}", email);
                return false;
            }

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
        OtpData storedData = getStoredOtp(email);

        if (storedData == null) {
            logger.warn("No OTP found for email: {}", email);
            return false;
        }

        // Check expiration
        if (System.currentTimeMillis() > storedData.expiryTime) {
            removeOtp(email);
            logger.warn("Expired OTP for email: {}", email);
            return false;
        }

        // Verify OTP
        boolean isValid = storedData.otpCode.equals(otpCode);

        if (isValid) {
            // Remove OTP after successful verification (one-time use)
            removeOtp(email);
            logger.info("OTP verified successfully for email: {}", email);
        } else {
            logger.warn("Invalid OTP attempt for email: {}", email);
        }

        return isValid;
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