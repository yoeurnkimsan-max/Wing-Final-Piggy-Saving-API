package com.example.piggy_saving.event;

import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.models.UserModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PiggyBrokenCompleteEvent extends ApplicationEvent {

    private final UserModel user;
    private final PiggyGoalModel piggyGoal;
    private final BigDecimal penaltyAmount;
    private final BigDecimal amountCredited;
    private final UUID transactionId;
    private final LocalDateTime transactionDate;

    public PiggyBrokenCompleteEvent(
            Object source,
            UserModel user,
            PiggyGoalModel piggyGoal,
            BigDecimal penaltyAmount,
            BigDecimal amountCredited,
            UUID transactionId
    ) {
        super(source);
        this.user = user;
        this.piggyGoal = piggyGoal;
        this.penaltyAmount = penaltyAmount;
        this.amountCredited = amountCredited;
        this.transactionId = transactionId;
        this.transactionDate = LocalDateTime.now(); // or pass from service if needed
    }
}