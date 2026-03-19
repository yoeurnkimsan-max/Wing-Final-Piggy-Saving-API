package com.example.piggy_saving.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class TransferOwnerResponse {
    private final String userName;
    private final String userEmail;
    private final String goalName;
    private final BigDecimal amount;
    private final BigDecimal newMainBalance;
    private final BigDecimal newPiggyBalance;
    private final Integer progressPercentage;
    private final boolean goalCompleted;
    private final String transactionId;
    private final LocalDateTime transactionDateTime;
    private final String sourceAccountMask;
    private final String destinationAccountMask;

    // Links (can be injected via properties)
    private final String goalLink;
    private final String walletLink;
    private final String transactionHistoryLink;
    private final String unsubscribeLink;
    private final String privacyPolicyLink;
    private final String appBaseUrl;

    public String getFormattedAmount() {
        return "$" + String.format("%,.2f", amount);
    }

    public String getFormattedMainBalance() {
        return "$" + String.format("%,.2f", newMainBalance);
    }

    public String getFormattedPiggyBalance() {
        return "$" + String.format("%,.2f", newPiggyBalance);
    }

    public String getFormattedDateTime() {
        if (transactionDateTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a");
        return transactionDateTime.format(formatter);
    }

    public Map<String, Object> toTemplateVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("appName", "Piggy Saving");
        vars.put("currentYear", LocalDateTime.now().getYear());
        vars.put("supportEmail", "support@piggysaving.com");

        vars.put("userName", userName);
        vars.put("goalName", goalName);
        vars.put("amount", getFormattedAmount());
        vars.put("newMainBalance", getFormattedMainBalance());
        vars.put("newPiggyBalance", getFormattedPiggyBalance());
        vars.put("progressPercentage", progressPercentage);
        vars.put("goalCompleted", goalCompleted);
        vars.put("transactionId", transactionId);
        vars.put("transactionDateTime", getFormattedDateTime());
        vars.put("sourceAccountMask", sourceAccountMask);
        vars.put("destinationAccountMask", destinationAccountMask);

        vars.put("goalLink", goalLink);
        vars.put("walletLink", walletLink);
        vars.put("transactionHistoryLink", transactionHistoryLink);
        vars.put("unsubscribeLink", unsubscribeLink + "?email=" + userEmail);
        vars.put("privacyPolicyLink", privacyPolicyLink);
        vars.put("emailSubject", "You transferred money to your goal: " + goalName);

        return vars;
    }
}