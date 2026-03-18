package com.example.piggy_saving.event;

import com.example.piggy_saving.models.UserModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class P2PTransferCompletedEvent extends ApplicationEvent {

    private final UserModel sender;
    private final UserModel recipient;
    private final BigDecimal amount;
    private final String description;
    private final UUID transactionId;

    public P2PTransferCompletedEvent(Object source,
                                     UserModel sender,
                                     UserModel recipient,
                                     BigDecimal amount,
                                     String description,
                                     UUID transactionId
                                     ) {
        super(source);
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.description = description;
        this.transactionId = transactionId;
    }
}
