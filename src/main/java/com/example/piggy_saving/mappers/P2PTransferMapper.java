package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.request.P2PTransferDataDto;
import com.example.piggy_saving.event.P2PTransferCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class P2PTransferMapper {

    @Value("${app.base-url:https://app.piggysaving.com}")
    private String baseUrl;

    // ADD ICON URL CONFIGURATION
    @Value("${app.icons.sender:https://cdn-icons-png.flaticon.com/256/13207/13207616.png}")
    private String senderIconUrl;

    @Value("${app.icons.receiver:https://cdn-icons-png.flaticon.com/256/13207/13207616.png}")
    private String receiverIconUrl;

    public P2PTransferDataDto toTransferData(P2PTransferCompletedEvent event) {
        return P2PTransferDataDto.builder()
                // Sender info
                .senderName(event.getSender().getName())
                .senderEmail(event.getSender().getEmail())
                .senderNewBalance(event.getSenderNewBalance().doubleValue())
                .sourceAccountName("Main Wallet")
                .sourceAccountMask(event.getSenderAccountMask())

                // Receiver info
                .receiverName(event.getReceiver().getName())
                .receiverEmail(event.getReceiver().getEmail())
                .receiverNewBalance(event.getReceiverNewBalance().doubleValue())
                .destinationWalletName("Main Wallet")
                .destinationAccountMask(event.getReceiverAccountMask())

                // Transaction info
                .amount(event.getAmount().doubleValue())
                .transactionId(event.getTransactionId().toString())
                .transactionDateTime(event.getTransactionDate())
                .personalMessage(event.getDescription())
                .currency("USD")

                // Links
                .transactionHistoryLink(baseUrl + "/transactions")
                .sendMoneyLink(baseUrl + "/send")
                .walletLink(baseUrl + "/wallet")
                .unsubscribeLink(baseUrl + "/unsubscribe")
                .privacyPolicyLink(baseUrl + "/privacy")
                .appBaseUrl(baseUrl)

                // ICON URL - SET HERE (you can make it dynamic based on event if needed)
                .headerIconUrl(senderIconUrl) // or receiverIconUrl based on context
                .build();
    }

    // Optional: If you need different icons for sender/receiver
    public P2PTransferDataDto toSenderData(P2PTransferCompletedEvent event) {
        P2PTransferDataDto dto = toTransferData(event);
        return P2PTransferDataDto.builder()
                .headerIconUrl(senderIconUrl)
                // copy all other fields from dto
                .build();
    }

    public P2PTransferDataDto toReceiverData(P2PTransferCompletedEvent event) {
        P2PTransferDataDto dto = toTransferData(event);
        return P2PTransferDataDto.builder()
                .headerIconUrl(receiverIconUrl)
                // copy all other fields from dto
                .build();
    }
}