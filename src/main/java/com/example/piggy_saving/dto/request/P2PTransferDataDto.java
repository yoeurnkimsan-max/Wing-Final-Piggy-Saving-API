package com.example.piggy_saving.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class P2PTransferDataDto {

    // SENDER fields
    private final String senderName;
    private final String senderEmail;
    private final Double senderNewBalance;
    private final String sourceAccountName;
    private final String sourceAccountMask;

    // RECEIVER fields
    private final String receiverName;
    private final String receiverEmail;
    private final Double receiverNewBalance;
    private final String destinationWalletName;
    private final String destinationAccountMask;

    // TRANSACTION fields
    private final Double amount;
    private final String transactionId;
    private final LocalDateTime transactionDateTime;
    private final String personalMessage;
    private final String currency;

    // LINKS
    private final String transactionHistoryLink;
    private final String sendMoneyLink;
    private final String walletLink;
    private final String unsubscribeLink;
    private final String privacyPolicyLink;
    private final String appBaseUrl;

    public String getFormattedAmount() {
        return amount == null ? "$0.00" : "$" + String.format("%,.2f", amount);
    }

    public String getFormattedSenderBalance() {
        return senderNewBalance == null ? "$0.00" : "$" + String.format("%,.2f", senderNewBalance);
    }

    public String getFormattedReceiverBalance() {
        return receiverNewBalance == null ? "$0.00" : "$" + String.format("%,.2f", receiverNewBalance);
    }

    public String getFormattedDateTime() {
        if (transactionDateTime == null) {
            return "N/A"; // or LocalDateTime.now()
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a");
        return transactionDateTime.format(formatter);
    }

    public Map<String, Object> toSenderVariables() {
        Map<String, Object> vars = new HashMap<>();

        vars.put("appName", "Piggy Saving");
        vars.put("currentYear", LocalDateTime.now().getYear());
        vars.put("supportEmail", "support@piggysaving.com");

        vars.put("senderName", senderName);
        vars.put("sourceAccountName", sourceAccountName);
        vars.put("sourceAccountMask", sourceAccountMask);
        vars.put("newBalance", getFormattedSenderBalance());

        vars.put("receiverName", receiverName);
        vars.put("receiverEmail", receiverEmail);

        vars.put("amount", getFormattedAmount());
        vars.put("transactionId", transactionId);
        vars.put("transactionDateTime", getFormattedDateTime());
        vars.put("personalMessage", personalMessage != null ? personalMessage : "");

        vars.put("transactionHistoryLink", transactionHistoryLink);
        vars.put("sendMoneyLink", sendMoneyLink);
        vars.put("walletLink", walletLink);
        vars.put("unsubscribeLink", unsubscribeLink + "?email=" + (senderEmail != null ? senderEmail : ""));
        vars.put("privacyPolicyLink", privacyPolicyLink);
        vars.put("emailSubject", "You sent " + getFormattedAmount() + " to " + receiverName);

        return vars;
    }

    public Map<String, Object> toReceiverVariables() {
        Map<String, Object> vars = new HashMap<>();

        vars.put("appName", "Piggy Saving");
        vars.put("currentYear", LocalDateTime.now().getYear());
        vars.put("supportEmail", "support@piggysaving.com");

        vars.put("receiverName", receiverName);
        vars.put("destinationWallet", destinationWalletName);
        vars.put("destinationAccountMask", destinationAccountMask);
        vars.put("newBalance", getFormattedReceiverBalance());

        vars.put("senderName", senderName);
        vars.put("senderEmail", senderEmail);

        vars.put("amount", getFormattedAmount());
        vars.put("transactionId", transactionId);
        vars.put("transactionDateTime", getFormattedDateTime());
        vars.put("personalMessage", personalMessage);

        vars.put("walletLink", walletLink);
        vars.put("sendMoneyLink", sendMoneyLink + "?to=" + senderEmail);
        vars.put("transactionHistoryLink", transactionHistoryLink);
        vars.put("unsubscribeLink", unsubscribeLink + "?email=" + receiverEmail);
        vars.put("privacyPolicyLink", privacyPolicyLink);
        vars.put("emailSubject", "You received " + getFormattedAmount() + " from " + senderName);

        return vars;
    }
}