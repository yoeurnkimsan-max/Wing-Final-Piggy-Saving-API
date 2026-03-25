package com.example.piggy_saving.services;

import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.TransactionModel;
import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.dto.request.TransferRequestDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {

    /**
     * Record an interest payment transaction.
     *
     * @param account The account receiving interest (piggy account)
     * @param amount  The interest amount to add
     * @return The created transaction
     */
    TransactionModel createInterestTransaction(AccountModel account, BigDecimal amount);


}