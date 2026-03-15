package com.example.piggy_saving.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailOtpService {

    private static final Logger logger = LoggerFactory.getLogger(EmailOtpService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String OTP_CACHE = "otpCache";

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private CacheManager cacheManager;

    // Fallback in-memory store if cache is not configured
    private final java.util.Map<String, OtpData> fallbackStore = new java.util.concurrent.ConcurrentHashMap<>();

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

            // Wait for email result (or make controller async later)
            Boolean emailSent = emailFuture.get();

            if (emailSent) {
                logger.info("OTP sent and stored for email: {}", email);
                return true;
            } else {
                // Remove stored OTP if email failed
                removeOtp(email);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error in sendOtp for email: {}", email, e);
            return false;
        }
    }

    /**
     * Store OTP with 5-minute expiration
     */
    private void storeOtp(String email, String otpCode) {
        if (cacheManager != null) {
            // Use Caffeine cache if available
            Cache cache = cacheManager.getCache(OTP_CACHE);
            if (cache != null) {
                cache.put(email, new OtpData(otpCode, System.currentTimeMillis() + 300000)); // 5 minutes
                return;
            }
        }

        // Fallback to in-memory store
        fallbackStore.put(email, new OtpData(otpCode, System.currentTimeMillis() + 300000));
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
            // Remove after successful verification (one-time use)
            removeOtp(email);
            logger.info("OTP verified successfully for email: {}", email);
        } else {
            logger.warn("Invalid OTP attempt for email: {}", email);
        }

        return isValid;
    }

    private OtpData getStoredOtp(String email) {
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(OTP_CACHE);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(email);
                if (wrapper != null) {
                    return (OtpData) wrapper.get();
                }
                return null;
            }
        }
        return fallbackStore.get(email);
    }

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
     * Inner class to hold OTP data
     */
    private static class OtpData {
        String otpCode;
        long expiryTime;

        OtpData(String otpCode, long expiryTime) {
            this.otpCode = otpCode;
            this.expiryTime = expiryTime;
        }
    }
}