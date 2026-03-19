package com.example.piggy_saving.event;

import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.models.UserModel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OwnTransferMainToPiggyCompletedEvent extends ApplicationEvent {

    private final UserModel owner;                    // The goal owner (user who owns both accounts)
    private final AccountModel sourceAccount;         // Main account (source)
    private final AccountModel destinationAccount;    // Piggy account (destination)
    private final PiggyGoalModel piggyGoal;           // The piggy goal
    private final BigDecimal amount;                   // Transfer amount
    private final String description;                  // Transfer description
    private final UUID transactionId;                  // Transaction ID
    private final LocalDateTime transactionDate;       // Transaction date/time
    private final BigDecimal newMainBalance;           // Main account new balance
    private final BigDecimal newPiggyBalance;          // Piggy account new balance
    private final String sourceAccountMask;            // Masked main account number
    private final String destinationAccountMask;       // Masked piggy account number
    private final boolean goalCompleted;                // Whether goal was completed by this transfer
    private final BigDecimal goalTargetAmount;          // Goal target amount
    private final BigDecimal goalProgress;              // Current progress toward goal
    private final Integer goalProgressPercentage;       // Progress percentage

    public OwnTransferMainToPiggyCompletedEvent(
            Object source,
            UserModel owner,
            AccountModel sourceAccount,
            AccountModel destinationAccount,
            PiggyGoalModel piggyGoal,
            BigDecimal amount,
            String description,
            UUID transactionId,
            LocalDateTime transactionDate,
            BigDecimal newMainBalance,
            BigDecimal newPiggyBalance,
            String sourceAccountMask,
            String destinationAccountMask,
            boolean goalCompleted
    ) {
        super(source);
        this.owner = owner;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.piggyGoal = piggyGoal;
        this.amount = amount;
        this.description = description;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.newMainBalance = newMainBalance;
        this.newPiggyBalance = newPiggyBalance;
        this.sourceAccountMask = sourceAccountMask;
        this.destinationAccountMask = destinationAccountMask;
        this.goalCompleted = goalCompleted;
        this.goalTargetAmount = piggyGoal.getTargetAmount();
        this.goalProgress = newPiggyBalance;

        // Calculate progress percentage
        if (piggyGoal.getTargetAmount() != null && piggyGoal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            this.goalProgressPercentage = newPiggyBalance.multiply(BigDecimal.valueOf(100))
                    .divide(piggyGoal.getTargetAmount(), 0, BigDecimal.ROUND_DOWN)
                    .intValue();
        } else {
            this.goalProgressPercentage = 0;
        }
    }
}
