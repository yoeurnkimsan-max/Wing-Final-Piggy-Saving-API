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
    private final BigDecimal newMainBalance;   // new main balance after credit
    private final BigDecimal originalBalance;  // original piggy balance before break
    private final BigDecimal penaltyRate;      // rate as fraction (e.g., 0.10)

    public PiggyBrokenCompleteEvent(
            Object source,
            UserModel user,
            PiggyGoalModel piggyGoal,
            BigDecimal penaltyAmount,
            BigDecimal amountCredited,
            UUID transactionId,
            LocalDateTime transactionDate,
            BigDecimal newMainBalance,
            BigDecimal originalBalance,
            BigDecimal penaltyRate
    ) {
        super(source);
        this.user = user;
        this.piggyGoal = piggyGoal;
        this.penaltyAmount = penaltyAmount;
        this.amountCredited = amountCredited;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.newMainBalance = newMainBalance;
        this.originalBalance = originalBalance;
        this.penaltyRate = penaltyRate;
    }
}