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
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Piggy Saving Verification</title>"
                + "</head>"

                + "<body style='margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,Helvetica,sans-serif;'>"

                + "<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#f4f6f8;padding:20px;'>"
                + "<tr>"
                + "<td align='center'>"

                + "<table width='520' cellpadding='0' cellspacing='0' border='0' style='background:white;border-radius:12px;overflow:hidden;'>"

                // HEADER
                + "<tr>"
                + "<td style='background:#111827;padding:24px;text-align:center;'>"
                + "<span style='color:white;font-size:24px;font-weight:bold;'>🐷 Piggy Saving</span>"
                + "</td>"
                + "</tr>"

                // BODY
                + "<tr>"
                + "<td style='padding:32px;'>"

                + "<p style='font-size:16px;color:#333;margin:0 0 16px;'>"
                + "Hello <strong>" + escapeHtml(name) + "</strong>,"
                + "</p>"

                + "<p style='font-size:15px;color:#555;line-height:1.6;margin:0 0 24px;'>"
                + "Thank you for using <strong>Piggy Saving</strong>. "
                + "Please use the One-Time Password (OTP) below to complete your verification."
                + "</p>"

                // OTP BOX
                + "<table width='100%' cellpadding='0' cellspacing='0' border='0'>"
                + "<tr>"
                + "<td align='center' style='padding:20px 0;'>"

                + "<div style='"
                + "display:inline-block;"
                + "padding:16px 28px;"
                + "font-size:36px;"
                + "letter-spacing:6px;"
                + "font-weight:bold;"
                + "background:#f3f4f6;"
                + "border-radius:8px;"
                + "color:#111827;"
                + "font-family:Courier New,monospace;'>"
                + escapeHtml(otpCode)
                + "</div>"

                + "</td>"
                + "</tr>"
                + "</table>"

                + "<p style='font-size:14px;color:#666;margin-top:20px;'>"
                + "This OTP will expire in <strong>5 minutes</strong>."
                + "</p>"

                + "<p style='font-size:14px;color:#666;line-height:1.6;'>"
                + "If you did not request this code, please ignore this email or contact our support team."
                + "</p>"

                + "</td>"
                + "</tr>"

                // FOOTER
                + "<tr>"
                + "<td style='background:#f9fafb;padding:20px;text-align:center;font-size:12px;color:#999;'>"

                + "<p style='margin:0;'>© 2026 Piggy Saving</p>"
                + "<p style='margin:4px 0;'>Secure Digital Saving Platform</p>"

                + "</td>"
                + "</tr>"

                + "</table>"

                + "</td>"
                + "</tr>"
                + "</table>"

                + "</body>"
                + "</html>";
    }

    // Helper method to escape HTML special characters for security
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}