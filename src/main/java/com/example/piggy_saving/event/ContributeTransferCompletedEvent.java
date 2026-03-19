package com.example.piggy_saving.event;

import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.models.UserModel;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContributeTransferCompletedEvent extends ApplicationEvent {

    private final UserModel user;
    private final PiggyGoalModel piggyGoal;
    private final BigDecimal amount;
    private final String description;
    private final UUID transactionId;
    private final LocalDateTime transactionDate;
    private final String notes;

    public ContributeTransferCompletedEvent(Object source,
                                            UserModel user,
                                            PiggyGoalModel piggyGoal,
                                            BigDecimal amount,
                                            String description,
                                            UUID transactionId,
                                            LocalDateTime transactionDate,
                                            String notes
    ) {
        super(source);
        this.user = user;
        this.piggyGoal = piggyGoal;
        this.amount = amount;
        this.description = description;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }

    // Getters
    public UserModel getUser() {
        return user;
    }

    public PiggyGoalModel getPiggyGoal() {
        return piggyGoal;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getNotes() {
        return notes;
    }
}
