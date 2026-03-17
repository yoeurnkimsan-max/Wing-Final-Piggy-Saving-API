package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.TransferP2PRequestDto;
import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.dto.response.TransferP2PResponseDto;
import com.example.piggy_saving.dto.response.TransferResponseDto;
import com.example.piggy_saving.exception.AccountNotFoundException;
import com.example.piggy_saving.exception.InsufficientBalanceException;
import com.example.piggy_saving.exception.SelfTransferNotAllowedException;
import com.example.piggy_saving.exception.UserNotFoundException;
import com.example.piggy_saving.models.*;
import com.example.piggy_saving.models.enums.*;
import com.example.piggy_saving.repository.*;
import com.example.piggy_saving.services.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

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
         * Find Existing User
         */
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        /**
         * Find Main Account with pessimistic lock
         */
        AccountModel mainAccount = accountRepository
                .findAccountModelsByUserModelIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new AccountNotFoundException("User main account not found"));

        BigDecimal transferAmount = transferRequestDto.getTransferAmount();

        /**
         * Validate transfer amount
         */
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        /**
         * Check balance
         */
        if (mainAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transfer");
        }

        /**
         * Find PiggyGoal with correct parameter order
         */
        PiggyGoalModel piggyGoal = piggyGoalRepository
                .findByIdAndUserModelId(transferRequestDto.getPiggyAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException("Piggy goal not found"));

        /**
         * Validate piggy goal status
         */
        if (piggyGoal.getStatus() != GoalStatus.ACTIVE) {
            throw new IllegalArgumentException("Piggy goal is not active");
        }

        /**
         * Find Piggy Account
         */
        AccountModel piggyAccount = accountRepository
                .findByPiggyGoalModelIdAndUserModelId(transferRequestDto.getPiggyAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException("Piggy account not found"));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Transfer to Piggy Goal: " + piggyGoal.getName());
        // 1️⃣ Create transaction with proper metadata
        TransactionModel transaction = TransactionModel.builder()
                .initiatedByUserModel(user)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .referenceId(UUID.randomUUID().toString())
                .metadata(metadata)
                .build();

        transaction = transactionRepository.save(transaction);

        // 2️⃣ Create ledger entries
        LedgerEntryModel debitEntry = LedgerEntryModel.builder()
                .transactionModel(transaction)
                .accountModel(mainAccount)
                .amount(transferAmount.negate())
                .entryType(EntryType.DEBIT)
                .build();

        LedgerEntryModel creditEntry = LedgerEntryModel.builder()
                .transactionModel(transaction)
                .accountModel(piggyAccount)
                .amount(transferAmount)
                .entryType(EntryType.CREDIT)
                .build();

        // Set bidirectional relationship
        transaction.setLedgerEntries(new ArrayList<>(List.of(debitEntry, creditEntry)));

        try {
            // 3️⃣ Update balances
            BigDecimal newMainBalance = mainAccount.getBalance().subtract(transferAmount);
            BigDecimal newPiggyBalance = piggyAccount.getBalance().add(transferAmount);

            mainAccount.setBalance(newMainBalance);
            piggyAccount.setBalance(newPiggyBalance);

            // Update piggy goal current balance to match account balance
            piggyGoal.setCurrentBalance(newPiggyBalance);

            // 4️⃣ Check if goal is completed
            boolean goalCompleted = false;
            if (newPiggyBalance.compareTo(piggyGoal.getTargetAmount()) >= 0) {
                piggyGoal.setStatus(GoalStatus.COMPLETED);
                piggyGoal.setCompletedAt(transaction.getCreatedAt());
                goalCompleted = true;
            }

            // 5️⃣ Save everything
            accountRepository.save(mainAccount);
            accountRepository.save(piggyAccount);
            piggyGoalRepository.save(piggyGoal);

            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            // ✅ SUCCESS
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            // 6️⃣ Build response with all required fields
            return TransferResponseDto.builder()
                    .transactionId(transaction.getId())
                    .fromAccountId(mainAccount.getId())
                    .completedAt(transaction.getCreatedAt())
                    .toAccountId(piggyAccount.getId())
                    .amount(transferAmount)
                    .type(TransactionType.TRANSFER)
                    .description("Transfer to Piggy Goal: " + piggyGoal.getName())
                    .newMainBalance(newMainBalance)
                    .newPiggyBalance(newPiggyBalance)
                    .goalCompleted(goalCompleted)
                    .completedAt(goalCompleted ? piggyGoal.getCompletedAt() : null)
                    .build();

        } catch (Exception e) {
            // ❌ FAILURE - mark transaction as failed
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferP2PResponseDto transferP2P(UUID userId, TransferP2PRequestDto transferRequestDto) {

        /**
         * Find Existing User
         */
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        /**
         * Find Main Account with pessimistic lock
         */
        AccountModel mainAccount = accountRepository
                .findAccountModelsByUserModelIdAndAccountType(user.getId(), AccountType.MAIN)
                .orElseThrow(() -> new AccountNotFoundException("User main account not found"));

        BigDecimal transferAmount = transferRequestDto.getAmount();

        /**
         * Validate transfer amount
         */
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        /**
         * Check balance
         */
        if (mainAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transfer");
        }

        /**
         * Find Recipient Account
         */
        UserModel recipientUser = userRepository.findById(transferRequestDto.getRecipientUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        /**
         * Find Recipient account
         */
        AccountModel recipientMainAccount = accountRepository
                .findAccountModelsByUserModelIdAndAccountType(recipientUser.getId(), AccountType.MAIN)
                .orElseThrow(() -> new AccountNotFoundException("User main account not found"));

        /**
         * Validate Sender account, Recipient account does it belongs to singe user?
         */
        if(mainAccount.getId().equals(recipientMainAccount.getId())){
            throw new SelfTransferNotAllowedException("You cannot transfer money to your own account");
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Transfer to Account: " + recipientUser.getName());
        // 1️⃣ Create transaction with proper metadata
        TransactionModel transaction = TransactionModel.builder()
                .initiatedByUserModel(user)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .referenceId(UUID.randomUUID().toString())
                .metadata(metadata)
                .build();

        transaction = transactionRepository.save(transaction);

        // 2️⃣ Create ledger entries
        LedgerEntryModel debitEntry = LedgerEntryModel.builder()
                .transactionModel(transaction)
                .accountModel(mainAccount)
                .amount(transferAmount.negate())
                .entryType(EntryType.DEBIT)
                .build();

        LedgerEntryModel creditEntry = LedgerEntryModel.builder()
                .transactionModel(transaction)
                .accountModel(recipientMainAccount)
                .amount(transferAmount)
                .entryType(EntryType.CREDIT)
                .build();

        // Set bidirectional relationship
        transaction.setLedgerEntries(new ArrayList<>(List.of(debitEntry, creditEntry)));

        try {
            // 3️⃣ Update sender balances
            BigDecimal newSenderMainBalance = mainAccount.getBalance().subtract(transferAmount);
            /**
             * Update new Recipient Main account Balance
             */
            BigDecimal newRecipientMainBalance = recipientMainAccount.getBalance().add(transferAmount);

            /**
             * Debit sender main balance
             */
            mainAccount.setBalance(newSenderMainBalance);

            /**
             * Credit recipient main balance
             */
            recipientMainAccount.setBalance(newRecipientMainBalance);



            //4️⃣ Save Sender main account and recipient
            accountRepository.save(mainAccount);
            accountRepository.save(recipientMainAccount);

            //5️⃣ Save debit entry and credit entry
            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            // ✅ SUCCESS: Save transaction
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            // 6️⃣ Build response with all required fields
            return TransferP2PResponseDto.builder()
                    .transactionId(transaction.getId())
                    .fromAccountId(mainAccount.getId())
                    .toAccountId(recipientMainAccount.getId())
                    .amount(transferAmount)
                    .type(TransactionType.P2P)
                    .recipientName(recipientUser.getName())
                    .description("Transfer P2P to " + recipientUser.getName())
                    .newMainBalance(newSenderMainBalance)
                    .completedAt(transaction.getCreatedAt())
                    .build();

        } catch (Exception e) {
            // ❌ FAILURE - mark transaction as failed
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }


}
