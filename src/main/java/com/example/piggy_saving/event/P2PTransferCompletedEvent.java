package com.example.piggy_saving.event;

import com.example.piggy_saving.models.UserModel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class P2PTransferCompletedEvent extends ApplicationEvent {

    private final UserModel sender;
    private final UserModel receiver;
    private final BigDecimal amount;
    private final String description;
    private final UUID transactionId;
    private final LocalDateTime transactionDate;
    private final BigDecimal senderNewBalance;
    private final BigDecimal receiverNewBalance;
    private final String senderAccountMask;
    private final String receiverAccountMask;

    public P2PTransferCompletedEvent(
            Object source,
            UserModel sender,
            UserModel receiver,
            BigDecimal amount,
            String description,
            UUID transactionId,
            LocalDateTime transactionDate,
            BigDecimal senderNewBalance,
            BigDecimal receiverNewBalance,
            String senderAccountMask,
            String receiverAccountMask
    ) {
        super(source);
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.description = description;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.senderNewBalance = senderNewBalance;
        this.receiverNewBalance = receiverNewBalance;
        this.senderAccountMask = senderAccountMask;
        this.receiverAccountMask = receiverAccountMask;
    }
}