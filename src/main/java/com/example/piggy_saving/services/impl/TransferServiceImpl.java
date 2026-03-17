package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.dto.response.TransferResponseDto;
import com.example.piggy_saving.exception.AccountNotFoundException;
import com.example.piggy_saving.exception.InsufficientBalanceException;
import com.example.piggy_saving.exception.UserNotFoundException;
import com.example.piggy_saving.models.*;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.models.enums.TransactionStatus;
import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.repository.*;
import com.example.piggy_saving.services.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PiggyGoalRepository piggyGoalRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponseDto transferToPiggy(UUID userId, TransferToPiggyRequestDto transferRequestDto) {

        /**
         * Find Exist User
         */
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        /**
         * Find Main Account
         */
        AccountModel mainAccount = accountRepository
                .findAccountModelsByUserModelIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new AccountNotFoundException("User main account not found"));

        BigDecimal transferAmount = transferRequestDto.getTransferAmount();

        /**
         * Check balance
         */
        if (mainAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transfer");
        }

        /**
         * Find PiggyGoal of PiggyModel with user ID where we want to credit
         */
        PiggyGoalModel piggyGoalAccount = piggyGoalRepository
                .findByIdAndUserModelId(userId, transferRequestDto.getPiggyAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Piggy account not found"));
        /**
         * Find Piggy Account  with piggy-id and user-id
         */
        AccountModel piggyAccount = accountRepository
                .findByPiggyGoalModelIdAndUserModelId(transferRequestDto.getPiggyAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException("Piggy Account not found"));

        // 1️⃣ Create transaction
        TransactionModel transaction = TransactionModel.builder()
                .initiatedByUserModel(user)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .referenceId(UUID.randomUUID().toString())
                .build();

        transaction = transactionRepository.save(transaction);

        // 2️⃣ Create ledger entries
        LedgerEntryModel debitEntry = LedgerEntryModel.builder()
                .accountModel(mainAccount)
                .amount(transferAmount.negate())
                .transactionModel(transaction)
                .build();

        LedgerEntryModel creditEntry = LedgerEntryModel.builder()
                .accountModel(piggyAccount)
                .amount(transferAmount)
                .transactionModel(transaction)
                .build();

        transaction.setLedgerEntries(List.of(debitEntry, creditEntry));

        try {
            // 3️⃣ Update balances safely
            mainAccount.setBalance(mainAccount.getBalance().subtract(transferAmount));
            piggyAccount.setBalance(piggyAccount.getBalance().add(transferAmount));
            piggyGoalAccount.setCurrentBalance(
                    piggyGoalAccount.getCurrentBalance().add(transferAmount)
            );

            // 4️⃣ Save everything
            accountRepository.save(mainAccount);
            accountRepository.save(piggyAccount);
            piggyGoalRepository.save(piggyGoalAccount);

            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            // ✅ SUCCESS
            transaction.setStatus(TransactionStatus.COMPLETED);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw e;
        } finally {
            transactionRepository.save(transaction);
        }

        TransferResponseDto responseDto = TransferResponseDto.builder()
                .transactionId(transaction.getId())
                .type(TransactionType.TRANSFER)
                .amount(transferAmount)
                .description("Transfer")
                .fromAccountId(mainAccount.getId())
                .toAccountId(piggyAccount.getId())
                .build();

        return responseDto;
    }
}
