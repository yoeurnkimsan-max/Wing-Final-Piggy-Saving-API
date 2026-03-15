package com.example.piggy_saving.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
        return "<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2\">"
                + "<div style=\"margin:50px auto;width:70%;padding:20px 0\">"
                + "<div style=\"border-bottom:1px solid #eee\">"
                + "<a href=\"\" style=\"font-size:1.4em;color: #00466a;text-decoration:none;font-weight:600\">Piggy Saving</a>"
                + "</div>"
                + "<p style=\"font-size:1.1em\">Hi, " + name + "</p>"
                + "<p>Thank you for choosing Piggy Saving. Use the following OTP to complete your verification. This OTP is valid for 5 minutes.</p>"
                + "<h2 style=\"background: #00466a;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" + otpCode + "</h2>"
                + "<p style=\"font-size:0.9em;\">Regards,<br />Piggy Saving Team</p>"
                + "<hr style=\"border:none;border-top:1px solid #eee\" />"
                + "</div>"
                + "</div>";
    }
}