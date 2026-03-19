package com.example.piggy_saving.event;

import com.example.piggy_saving.models.enums.TransferType;
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

    /**
     * Handle P2P transfers
     */
    @EventListener
    public void handleTransfer(P2PTransferCompletedEvent event) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedAmount = currencyFormat.format(event.getAmount());

        // ================= SENDER =================
        notificationService.notify(
                event.getSender(),
                String.format("You sent %s to %s. %s Transaction ID: %s",
                        formattedAmount,
                        event.getRecipient().getEmail(),
                        event.getDescription() != null ? "Description: " + event.getDescription() + "." : "",
                        event.getTransactionId())
        );

        emailService.sendTransferEmail(
                event.getSender().getName(),
                event.getSender().getEmail(),
                formattedAmount, // ✅ use formatted
                event.getRecipient().getName(),
                event.getRecipient().getEmail(),
                TransferType.P2P,
                event.getTransactionId(),
                event.getTransactionDate(),
                "",
                event.getDescription() != null ? event.getDescription() : ""
        );

        // ================= RECEIVER =================
        notificationService.notify(
                event.getRecipient(),
                String.format("You received %s from %s. %s Transaction ID: %s",
                        formattedAmount,
                        event.getSender().getEmail(),
                        event.getDescription() != null ? "Description: " + event.getDescription() + "." : "",
                        event.getTransactionId())
        );

        emailService.sendTransferEmail(
                event.getRecipient().getName(),
                event.getRecipient().getEmail(),
                formattedAmount,
                event.getSender().getName(),
                event.getSender().getEmail(),
                TransferType.P2P,
                event.getTransactionId(),
                event.getTransactionDate(),
                "",
                event.getDescription() != null ? event.getDescription() : ""
        );
    }

    /**
     * Handle contributions (Main → Piggy/Goal)
     */
    @EventListener
    public void handleContributeTransfer(ContributeTransferCompletedEvent event) {

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedAmount = currencyFormat.format(event.getAmount());

        // ================= CONTRIBUTOR =================
        notificationService.notify(
                event.getUser(),
                String.format("You contributed %s to goal %s. %s Transaction ID: %s",
                        formattedAmount,
                        event.getPiggyGoal().getName(),
                        event.getDescription() != null ? "Description: " + event.getDescription() + "." : "",
                        event.getTransactionId())
        );

        emailService.sendTransferEmail(
                event.getUser().getName(), // 👈 user is recipient of this email
                event.getUser().getEmail(),
                formattedAmount,
                event.getPiggyGoal().getName(), // 👈 treat as "goalName" later in template
                event.getPiggyGoal().getUserModel().getEmail(),
                TransferType.CONTRIBUTION,
                event.getTransactionId(),
                event.getTransactionDate(),
                event.getPiggyGoal().getName(),
                event.getNotes() != null ? event.getNotes() : ""
        );

        // ================= GOAL OWNER =================
        notificationService.notify(
                event.getPiggyGoal().getUserModel(),
                String.format("You received %s from %s for goal %s. %s Transaction ID: %s",
                        formattedAmount,
                        event.getUser().getEmail(),
                        event.getPiggyGoal().getName(),
                        event.getNotes() != null ? event.getNotes() : "",
                        event.getTransactionId())
        );

        emailService.sendTransferEmail(
                event.getPiggyGoal().getUserModel().getName(), // ✅ FIXED
                event.getPiggyGoal().getUserModel().getEmail(),
                formattedAmount,
                event.getUser().getName(),
                event.getUser().getEmail(),
                TransferType.CONTRIBUTION,
                event.getTransactionId(),
                event.getTransactionDate(),
                event.getPiggyGoal().getName(),
                event.getNotes() != null ? event.getNotes() : ""
        );
    }
}