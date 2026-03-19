package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.P2PTransferDataDto;
import com.example.piggy_saving.event.OwnTransferMainToPiggyCompletedEvent;
import com.example.piggy_saving.models.enums.TransferType;
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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
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

    @Value("${app.base-url}")
    private String baseUrl;

    // If you want individual link properties (optional)
    @Value("${app.links.goals:${app.base-url}/goals}")
    private String goalsLink;

    @Value("${app.links.wallet:${app.base-url}/wallet}")
    private String walletLink;

    @Value("${app.links.transactions:${app.base-url}/transactions}")
    private String transactionsLink;

    @Value("${app.links.unsubscribe:${app.base-url}/unsubscribe}")
    private String unsubscribeBaseLink;

    @Value("${app.links.privacy:${app.base-url}/privacy}")
    private String privacyLink;

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
    public CompletableFuture<Void> sendP2PTransferEmail(P2PTransferDataDto data, boolean isSender) {

        String toEmail = isSender ? data.getSenderEmail() : data.getReceiverEmail();

        try {
            logger.info("Preparing P2P {} email to {}", isSender ? "sender" : "receiver", toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            // Variables from DTO
            Map<String, Object> variables = isSender
                    ? data.toSenderVariables()
                    : data.toReceiverVariables();

            // ✅ Safe subject fallback
            String subject = (String) variables.getOrDefault(
                    "emailSubject",
                    isSender ? "Money Sent Successfully" : "Money Received Successfully"
            );

            helper.setSubject(subject);

            // Template selection
            String templateName = isSender
                    ? "email/p2p-sender-transfer"
                    : "email/p2p-receiver-transfer";

            logger.info("Using template: {}", templateName);

            String htmlContent = templateService.renderTemplate(templateName, variables);

            helper.setText(htmlContent, true);

            mailSender.send(message);

            logger.info("✅ P2P {} email sent successfully to: {}",
                    isSender ? "sender" : "receiver", toEmail);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {

            logger.error("❌ Failed to send P2P {} email to: {}",
                    isSender ? "sender" : "receiver",
                    toEmail,
                    e
            );

            // ⚠️ IMPORTANT: still return future to avoid async crash
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public void sendOwnTransferEmail(OwnTransferMainToPiggyCompletedEvent event) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("appName", APP_NAME);
            variables.put("currentYear", LocalDateTime.now().getYear());
            variables.put("supportEmail", SUPPORT_EMAIL);

            // User & goal info
            variables.put("userName", event.getOwner().getName());
            variables.put("goalName", event.getPiggyGoal().getName());
            variables.put("amount", formatCurrency(event.getAmount()));
            variables.put("newMainBalance", formatCurrency(event.getNewMainBalance()));
            variables.put("newPiggyBalance", formatCurrency(event.getNewPiggyBalance()));
            variables.put("progressPercentage", event.getGoalProgressPercentage());
            variables.put("goalCompleted", event.isGoalCompleted());
            variables.put("transactionId", event.getTransactionId().toString());
            variables.put("transactionDateTime", formatDateTime(event.getTransactionDate()));
            variables.put("sourceAccountMask", event.getSourceAccountMask());
            variables.put("destinationAccountMask", event.getDestinationAccountMask());
            variables.put("goalTargetAmount", formatCurrency(event.getPiggyGoal().getTargetAmount()));

            // Links – injected from properties (see below)
            variables.put("goalLink", baseUrl + "/" + event.getPiggyGoal().getId());
            variables.put("walletLink", walletLink);
            variables.put("transactionHistoryLink", transactionsLink);
            variables.put("unsubscribeLink", unsubscribeBaseLink + "?email=" + event.getOwner().getEmail());
            variables.put("privacyPolicyLink", privacyLink);
            variables.put("emailSubject", "You transferred money to your goal: " + event.getPiggyGoal().getName());

            // Render and send
            String htmlContent = templateService.renderTemplate("email/own-transfer", variables);
            sendHtmlEmail(event.getOwner().getEmail(), (String) variables.get("emailSubject"), htmlContent);

        } catch (Exception e) {
            logger.error("Failed to send own transfer email to: {}", event.getOwner().getEmail(), e);
            // Depending on your policy, you may rethrow or just log
        }
    }

    // Helper methods
    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }


    private void sendHtmlEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a");
        return dateTime.format(formatter);
    }
}