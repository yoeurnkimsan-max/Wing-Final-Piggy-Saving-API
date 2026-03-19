package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.P2PTransferDataDto;
import com.example.piggy_saving.models.enums.TransferType;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateService templateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String APP_NAME = "Piggy Saving";
    private static final String SUPPORT_EMAIL = "support@piggysaving.com";

    // ================= OTP EMAIL =================
    @Async
    public CompletableFuture<Boolean> sendOtpEmail(String toEmail, String otpCode, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your OTP Verification Code");

            Map<String, Object> variables = Map.of(
                    "userName", userName,
                    "otpCode", otpCode
            );

            String htmlContent = templateService.renderTemplate("email/otp", variables);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);

            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    // ================= TRANSFER EMAIL (for contributions and own transfers) =================
    @Async
    public void sendTransferEmail(
            String recipientName,
            String recipientEmail,
            String amount,
            String counterPartyName,
            String counterPartyEmail,
            TransferType type,
            UUID transactionId,
            LocalDateTime transactionDate,
            String goalName,
            String description
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);

            String subject = type.name() + " Transaction Notification";
            helper.setSubject(subject);

            String formattedDate = transactionDate.format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
            );

            Map<String, Object> variables = Map.ofEntries(
                    Map.entry("appName", APP_NAME),
                    Map.entry("emailSubject", "Contribution Received"),
                    Map.entry("recipientName", recipientName),
                    Map.entry("senderName", counterPartyName),
                    Map.entry("senderEmail", counterPartyEmail),
                    Map.entry("goalName", goalName),
                    Map.entry("amount", amount),
                    Map.entry("transactionDate", formattedDate),
                    Map.entry("transactionId", transactionId.toString()),
                    Map.entry("goalLink", "https://yourapp.com/goals"),
                    Map.entry("supportEmail", SUPPORT_EMAIL),
                    Map.entry("unsubscribeLink", "#"),
                    Map.entry("privacyPolicyLink", "#"),
                    Map.entry("currentYear", Year.now().getValue()),
                    Map.entry("description", description != null ? description : "")
            );

            String templateName = templateService.resolveTemplate(type);
            String htmlContent = templateService.renderTemplate(templateName, variables);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Transfer email sent successfully to: {}", recipientEmail);

        } catch (Exception e) {
            logger.error("Failed to send transfer email to: {}", recipientEmail, e);
            throw new RuntimeException("Failed to send transfer email", e);
        }
    }

    // ================= NEW: P2P TRANSFER EMAIL (supports both sender and receiver) =================
    @Async
    public void sendP2PTransferEmail(P2PTransferDataDto data, boolean isSender) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            String toEmail = isSender ? data.getSenderEmail() : data.getReceiverEmail();
            helper.setTo(toEmail);

            // Get the pre-built variables from the DTO
            Map<String, Object> variables = isSender ? data.toSenderVariables() : data.toReceiverVariables();
            helper.setSubject((String) variables.get("emailSubject"));

            // Choose the correct template
            String templateName = isSender ? "email/p2p-sender" : "email/p2p-receiver";
            String htmlContent = templateService.renderTemplate(templateName, variables);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("P2P {} email sent successfully to: {}", isSender ? "sender" : "receiver", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send P2P email to: {}", isSender ? data.getSenderEmail() : data.getReceiverEmail(), e);
            throw new RuntimeException("Failed to send P2P email", e);
        }
    }
}