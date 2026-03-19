package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.request.P2PTransferDataDto;
import com.example.piggy_saving.event.P2PTransferCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class P2PTransferMapper {

    @Value("${app.base-url:https://app.piggysaving.com}")
    private String baseUrl;

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
                .build();
    }
}