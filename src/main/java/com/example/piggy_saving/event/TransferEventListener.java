package com.example.piggy_saving.event;

import com.example.piggy_saving.services.EmailService;
import com.example.piggy_saving.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TransferEventListener {
    private final NotificationService notificationService;
    private final EmailService emailService;

    @EventListener
    public void handleTransfer(P2PTransferCompletedEvent event) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedAmount = currencyFormat.format(event.getAmount());
        /**
         * Notify sender
         */
        notificationService.notify(
                event.getSender(),
                String.format("You sent %s to %s. %s Transaction ID: %s",
                        formattedAmount,
                        event.getRecipient().getEmail(),
                        event.getDescription() != null ? "Description: " + event.getDescription() + "." : "",
                        event.getTransactionId())
        );
        emailService.sendEmail(
                event.getSender().getName(),             // recipientName
                event.getSender().getEmail(),            // recipientEmail
                event.getAmount().toString(),            // amount
                event.getRecipient().getName(),          // counterPartyName
                event.getRecipient().getEmail(),         // counterPartyEmail
                "SENT",                                  // type
                event.getTransactionId()                 // transactionId
        );

//        emailService.se


        /**
         * Notify recipient
         */
        notificationService.notify(
                event.getRecipient(),
                String.format("You received %s from %s. %s Transaction ID: %s",
                        formattedAmount,
                        event.getSender().getEmail(),
                        event.getDescription() != null ? "Description: " + event.getDescription() + "." : "",
                        event.getTransactionId())
        );
        emailService.sendEmail(
                event.getRecipient().getName(),          // recipientName
                event.getRecipient().getEmail(),         // recipientEmail
                event.getAmount().toString(),            // amount
                event.getSender().getName(),             // counterPartyName
                event.getSender().getEmail(),            // counterPartyEmail
                "RECEIVED",                              // type
                event.getTransactionId()                 // transactionId
        );

    }
}
