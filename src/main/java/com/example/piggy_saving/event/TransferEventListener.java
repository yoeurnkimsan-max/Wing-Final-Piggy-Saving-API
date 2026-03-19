package com.example.piggy_saving.event;

import com.example.piggy_saving.dto.request.P2PTransferDataDto;
import com.example.piggy_saving.mappers.P2PTransferMapper;
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
    private final P2PTransferMapper p2PTransferMapper;

    /**
     * Handle P2P transfers
     */
    @EventListener
    public void handleP2PTransfer(P2PTransferCompletedEvent event) {

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedAmount = currencyFormat.format(event.getAmount());

        // Notifications
        notificationService.notify(
                event.getSender(),
                "You sent " + formattedAmount + " to " + event.getReceiver().getEmail()
        );

        notificationService.notify(
                event.getReceiver(),
                "You received " + formattedAmount + " from " + event.getSender().getEmail()
        );

        // Build DTO safely
        P2PTransferDataDto data = P2PTransferDataDto.builder()
                .senderName(event.getSender().getName())
                .senderEmail(event.getSender().getEmail())
                .senderNewBalance(
                        event.getSenderNewBalance() != null
                                ? event.getSenderNewBalance().doubleValue()
                                : 0.0
                )
                .sourceAccountName("Main Wallet")
                .sourceAccountMask(event.getSenderAccountMask())
                .receiverName(event.getReceiver().getName())
                .receiverEmail(event.getReceiver().getEmail())
                .receiverNewBalance(
                        event.getReceiverNewBalance() != null
                                ? event.getReceiverNewBalance().doubleValue()
                                : 0.0
                )
                .destinationWalletName("Main Wallet")
                .destinationAccountMask(event.getReceiverAccountMask())
                .amount(event.getAmount().doubleValue())
                .transactionId(event.getTransactionId().toString())
                .transactionDateTime(event.getTransactionDate())
                .personalMessage(
                        event.getDescription() != null ? event.getDescription() : ""
                )
                .currency("USD")
                .transactionHistoryLink("https://app.piggysaving.com/transactions")
                .sendMoneyLink("https://app.piggysaving.com/send")
                .walletLink("https://app.piggysaving.com/wallet")
                .unsubscribeLink("https://app.piggysaving.com/unsubscribe")
                .privacyPolicyLink("https://piggysaving.com/privacy")
                .appBaseUrl("https://app.piggysaving.com")
                .build();

        // Send emails
        emailService.sendP2PTransferEmail(data, true);
        emailService.sendP2PTransferEmail(data, false);
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
                event.getNotes()
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