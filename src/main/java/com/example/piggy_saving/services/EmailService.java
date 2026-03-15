package com.example.piggy_saving.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public CompletableFuture<Boolean> sendOtpEmail(String toEmail, String otpCode, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your OTP Verification Code");
            helper.setText(buildEmailTemplate(userName, otpCode), true); // true = HTML

            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);

            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to: {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private String buildEmailTemplate(String name, String otpCode) {
        return "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Piggy Saving OTP – UI Preview</title>"
                + "<style>"
                + "body {"
                + "    background: #e9eef2;"
                + "    font-family: 'Segoe UI', Roboto, system-ui, sans-serif;"
                + "    display: flex;"
                + "    justify-content: center;"
                + "    align-items: center;"
                + "    min-height: 100vh;"
                + "    margin: 20px;"
                + "}"
                + ".preview-container {"
                + "    max-width: 700px;"
                + "    width: 100%;"
                + "    background: white;"
                + "    border-radius: 16px;"
                + "    box-shadow: 0 20px 40px -10px rgba(0,0,0,0.2);"
                + "    overflow: hidden;"
                + "}"
                + ".controls {"
                + "    background: #f8fafc;"
                + "    padding: 20px 24px;"
                + "    border-bottom: 1px solid #dde3e9;"
                + "    display: flex;"
                + "    gap: 20px;"
                + "    flex-wrap: wrap;"
                + "}"
                + ".control-group {"
                + "    display: flex;"
                + "    flex-direction: column;"
                + "    gap: 6px;"
                + "    flex: 1 1 200px;"
                + "}"
                + ".control-group label {"
                + "    font-weight: 600;"
                + "    font-size: 0.9rem;"
                + "    color: #1e3a5f;"
                + "}"
                + ".control-group input {"
                + "    padding: 10px 14px;"
                + "    border: 1px solid #bcccd9;"
                + "    border-radius: 8px;"
                + "    font-size: 1rem;"
                + "    transition: 0.15s;"
                + "}"
                + ".control-group input:focus {"
                + "    border-color: #00466a;"
                + "    outline: none;"
                + "    box-shadow: 0 0 0 3px rgba(0,70,106,0.2);"
                + "}"
                + ".email-frame {"
                + "    background: #f4f4f7;"
                + "    padding: 30px 20px;"
                + "}"
                + ".email-card {"
                + "    max-width: 600px;"
                + "    margin: 0 auto;"
                + "    background-color: #ffffff;"
                + "    border-radius: 8px;"
                + "    box-shadow: 0 4px 12px rgba(0,0,0,0.05);"
                + "    font-family: Arial, sans-serif;"
                + "}"
                + ".email-header {"
                + "    text-align: center;"
                + "    padding: 20px 0;"
                + "    background-color: #00466a;"
                + "    border-radius: 8px 8px 0 0;"
                + "}"
                + ".email-header h1 {"
                + "    color: #ffffff;"
                + "    margin: 0;"
                + "    font-size: 24px;"
                + "}"
                + ".email-body {"
                + "    padding: 30px;"
                + "    color: #333;"
                + "}"
                + ".email-body p {"
                + "    margin: 0 0 20px 0;"
                + "    font-size: 16px;"
                + "    line-height: 1.6;"
                + "    color: #555;"
                + "}"
                + ".otp-box {"
                + "    text-align: center;"
                + "    margin: 30px 0;"
                + "}"
                + ".otp-code {"
                + "    font-size: 28px;"
                + "    font-weight: bold;"
                + "    background-color: #00466a;"
                + "    color: #ffffff;"
                + "    padding: 15px 35px;"
                + "    border-radius: 6px;"
                + "    letter-spacing: 4px;"
                + "    display: inline-block;"
                + "}"
                + ".footer-note {"
                + "    font-size: 14px;"
                + "    color: #777;"
                + "    text-align: center;"
                + "    margin: 10px 0;"
                + "}"
                + ".email-footer {"
                + "    text-align: center;"
                + "    padding: 15px;"
                + "    font-size: 12px;"
                + "    color: #999;"
                + "    background-color: #f4f4f7;"
                + "    border-radius: 0 0 8px 8px;"
                + "}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='preview-container'>"
                + "<div class='controls'>"
                + "<div class='control-group'>"
                + "<label for='nameInput'>Recipient Name</label>"
                + "<input type='text' id='nameInput' value='" + name + "' placeholder='e.g. John Doe'>"
                + "</div>"
                + "<div class='control-group'>"
                + "<label for='otpInput'>OTP Code</label>"
                + "<input type='text' id='otpInput' value='" + otpCode + "' placeholder='6-digit code' maxlength='6'>"
                + "</div>"
                + "</div>"
                + "<div class='email-frame'>"
                + "<div class='email-card'>"
                + "<div class='email-header'>"
                + "<h1>Piggy Saving</h1>"
                + "</div>"
                + "<div class='email-body'>"
                + "<p>Hi <strong id='displayName'>" + name + "</strong>,</p>"
                + "<p>"
                + "Thank you for choosing Piggy Saving. Please use the OTP below to complete your verification. "
                + "<strong>This OTP is valid for 5 minutes.</strong>"
                + "</p>"
                + "<div class='otp-box'>"
                + "<span class='otp-code' id='displayOtp'>" + otpCode + "</span>"
                + "</div>"
                + "<p class='footer-note'>If you didn't request this OTP, please ignore this email.</p>"
                + "<p class='footer-note'>Regards,<br/>Piggy Saving Team</p>"
                + "</div>"
                + "<div class='email-footer'>"
                + "&copy; 2026 Piggy Saving. All rights reserved.<br/>123 Piggy St, Finance City, Country"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "<script>"
                + "const nameInput = document.getElementById('nameInput');"
                + "const otpInput = document.getElementById('otpInput');"
                + "const displayName = document.getElementById('displayName');"
                + "const displayOtp = document.getElementById('displayOtp');"
                + "function updatePreview() {"
                + "    displayName.textContent = nameInput.value || 'User';"
                + "    displayOtp.textContent = otpInput.value || '000000';"
                + "}"
                + "nameInput.addEventListener('input', updatePreview);"
                + "otpInput.addEventListener('input', updatePreview);"
                + "updatePreview();"
                + "</script>"
                + "</body></html>";
    }
}