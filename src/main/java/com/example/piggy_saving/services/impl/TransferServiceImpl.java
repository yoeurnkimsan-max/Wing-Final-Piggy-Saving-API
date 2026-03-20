package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.TransferBreakRequestDto;
import com.example.piggy_saving.dto.request.TransferContributeRequestDto;
import com.example.piggy_saving.dto.request.TransferP2PRequestDto;
import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.dto.response.TransferBreakPiggyResponseDto;
import com.example.piggy_saving.dto.response.TransferContributeResponseDto;
import com.example.piggy_saving.dto.response.TransferP2PResponseDto;
import com.example.piggy_saving.dto.response.TransferResponseDto;
import com.example.piggy_saving.event.ContributeTransferCompletedEvent;
import com.example.piggy_saving.event.OwnTransferMainToPiggyCompletedEvent;
import com.example.piggy_saving.event.P2PTransferCompletedEvent;
import com.example.piggy_saving.event.PiggyBrokenCompleteEvent;
import com.example.piggy_saving.exception.*;
import com.example.piggy_saving.models.*;
import com.example.piggy_saving.models.enums.*;
import com.example.piggy_saving.repository.*;
import com.example.piggy_saving.services.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PiggyGoalRepository piggyGoalRepository;
    private final UserRepository userRepository;

    /**
     * Notfiy Event
     *
     * @param userId
     * @param transferRequestDto
     * @return
     */
    private final ApplicationEventPublisher applicationEventPublisher;


    /**
     * OWN: Transfer to piggy account
     *
     * @param userId
     * @param transferRequestDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponseDto transferToPiggy(UUID userId, TransferToPiggyRequestDto transferRequestDto) {

        /**
         * Find Existing User
         */
        UserModel ownerUser = userRepository.findById(userId)
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
        AccountModel piggyAccount = accountRepository
                .findByAccountNumberAndUserModelId(transferRequestDto.getAccountPiggyNumber(), ownerUser.getId())
                .orElseThrow(() -> new AccountNotFoundException("Your Piggy Account not found"));
        /**
         * Validate piggy goal status
         */
        if (piggyAccount.getPiggyGoalModel().getStatus() != GoalStatus.ACTIVE) {
            throw new IllegalArgumentException("Piggy goal is not active");
        }

        /**
         * Find Piggy Account
         */
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Transfer to Piggy Goal: " + piggyAccount.getPiggyGoalModel().getName());
        // 1️⃣ Create transaction with proper metadata
        TransactionModel transaction = TransactionModel.builder()
                .initiatedByUserModel(ownerUser)
                .transactionType(TransactionType.GOAL_CONTRIBUTION)
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
            piggyAccount.getPiggyGoalModel().setCurrentBalance(newPiggyBalance);

            // 4️⃣ Check if goal is completed
            boolean goalCompleted = false;
            if (newPiggyBalance.compareTo(piggyAccount.getPiggyGoalModel().getTargetAmount()) >= 0) {
                piggyAccount.getPiggyGoalModel().setStatus(GoalStatus.COMPLETED);
                piggyAccount.getPiggyGoalModel().setCompletedAt(transaction.getCreatedAt());
                goalCompleted = true;
            }

            // 5️⃣ Save everything
            accountRepository.save(mainAccount);
            accountRepository.save(piggyAccount);

            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            // ✅ SUCCESS
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            // After updating balances and before returning response
            applicationEventPublisher.publishEvent(
                    new OwnTransferMainToPiggyCompletedEvent(
                            this,
                            ownerUser,                                      // owner (UserModel)
                            mainAccount,                                    // sourceAccount (AccountModel) - FIXED
                            piggyAccount,                                   // destinationAccount (AccountModel)
                            piggyAccount.getPiggyGoalModel(),               // piggyGoal (PiggyGoalModel)
                            transferAmount,                                 // amount
                            "Transfer to Piggy Goal: " + piggyAccount.getPiggyGoalModel().getName(), // description
                            transaction.getId(),                            // transactionId
                            transaction.getCreatedAt(),                     // transactionDate
                            newMainBalance,                                  // newMainBalance
                            newPiggyBalance,                                 // newPiggyBalance
                            maskAccountNumber(mainAccount.getAccountNumber()),           // sourceAccountMask
                            maskAccountNumber(piggyAccount.getAccountNumber()),          // destinationAccountMask
                            goalCompleted                                     // goalCompleted
                    )
            );

            // 6️⃣ Build response with all required fields
            return TransferResponseDto.builder()
                    .transactionId(transaction.getId())
                    .fromAccountId(mainAccount.getId())
                    .toAccountNumber(piggyAccount.getAccountNumber())
                    .fromAccountNumber(mainAccount.getAccountNumber())
                    .completedAt(transaction.getCreatedAt())
                    .toAccountId(piggyAccount.getId())
                    .amount(transferAmount)
                    .type(TransferType.OWN)
                    .description("Transfer to Piggy Goal: " + piggyAccount.getPiggyGoalModel().getName())
                    .newMainBalance(newMainBalance)
                    .newPiggyBalance(newPiggyBalance)
                    .goalCompleted(goalCompleted)
                    .completedAt(goalCompleted ? piggyAccount.getPiggyGoalModel().getCompletedAt() : null)
                    .build();

        } catch (Exception e) {
            // ❌ FAILURE - mark transaction as failed
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }


    /**
     * P2P Transfer
     *
     * @param userId
     * @param transferRequestDto
     * @return
     */
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

        AccountModel recipientUserAccount = accountRepository.findPublicMainAccountByAccountNumber(transferRequestDto.getRecipientAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Receiver Account number not found or Account number isn't correct."));


        /**
         * Validate Sender account, Recipient account does it belongs to singe user?
         */
        if (mainAccount.getId().equals(recipientUserAccount.getId())) {
            throw new SelfTransferNotAllowedException("You cannot transfer money to your own account");
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Transfer to Account: " + recipientUserAccount.getUserModel().getName());
        // 1️⃣ Create transaction with proper metadata
        TransactionModel transaction = TransactionModel.builder()
                .initiatedByUserModel(user)
                .transactionType(TransactionType.P2P_TRANSFER)
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
                .accountModel(recipientUserAccount)
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
            BigDecimal newRecipientMainBalance = recipientUserAccount.getBalance().add(transferAmount);

            /**
             * Debit sender main balance
             */
            mainAccount.setBalance(newSenderMainBalance);

            /**
             * Credit recipient main balance
             */
            recipientUserAccount.setBalance(newRecipientMainBalance);


            //4️⃣ Save Sender main account and recipient
            accountRepository.save(mainAccount);
            accountRepository.save(recipientUserAccount);

            //5️⃣ Save debit entry and credit entry
            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            // ✅ SUCCESS: Save transaction
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCreatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            applicationEventPublisher.publishEvent(
                    new P2PTransferCompletedEvent(
                            this,
                            user,                                   // sender
                            recipientUserAccount.getUserModel(),                          // receiver
                            transferAmount,                         // amount
                            "Transfer to " + recipientUserAccount.getUserModel().getEmail(), // description
                            transaction.getId(),                     // transaction ID
                            transaction.getCreatedAt(),              // date
                            newSenderMainBalance,                    // sender's new balance
                            newRecipientMainBalance,                  // receiver's new balance
                            maskAccountNumber(mainAccount.getAccountNumber()),           // sender account mask
                            maskAccountNumber(recipientUserAccount.getAccountNumber())   // receiver account mask
                    )
            );

            // 6️⃣ Build response with all required fields
            return TransferP2PResponseDto.builder()
                    .transactionId(transaction.getId())
                    .fromAccountId(mainAccount.getId())
                    .fromAccountNumber(mainAccount.getAccountNumber())
                    .toAccountId(recipientUserAccount.getId())
                    .toAccountNumber(recipientUserAccount.getAccountNumber())
                    .amount(transferAmount)
                    .type(TransferType.P2P)
                    .recipientName(recipientUserAccount.getUserModel().getName())
                    .description("Transfer P2P to " + recipientUserAccount.getUserModel().getName())
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


    /**
     * Hidden Balance response
     *
     * @param accountNumber
     * @return
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return "••••";
        return "•••• " + accountNumber.substring(accountNumber.length() - 4);
    }

    /**
     * Contribute transfer
     *
     * @param userId
     * @param transferRequestDto
     * @return TransferContributeResponseDto
     */
    @Override
    @Transactional
    public TransferContributeResponseDto transferContribute(UUID userId, TransferContributeRequestDto transferRequestDto) {
        /**
         * Find Existing User
         */
        UserModel senderUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Sender User not found"));

        /**
         * Find Main Account with pessimistic lock
         */
        AccountModel senderMainAccount = accountRepository
                .findAccountModelsByUserModelIdAndAccountType(senderUser.getId(), AccountType.MAIN)
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
        if (senderMainAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transfer");
        }

        /**
         * Find Recipient piggy account public
         */

        AccountModel recipientPiggyAccount = accountRepository.findByAccountNumberAndIsPublicTrue(transferRequestDto.getPiggyAccountNumber(), true)
                .orElseThrow(() -> new AccountNotFoundException("Piggy Account not found or Piggy account is private."));

        if(recipientPiggyAccount.getPiggyGoalModel().getStatus() == GoalStatus.BROKEN){
            throw new PiggyAccountBrokenException("Piggy goal " + recipientPiggyAccount.getPiggyGoalModel().getName() + " is already broken. Contribution is not allowed.");
        }


        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Transfer to Piggy Account: " + recipientPiggyAccount.getPiggyGoalModel().getName());
        // 1️⃣ Create transaction with proper metadata
        TransactionModel transaction = TransactionModel.builder()
                .initiatedByUserModel(senderUser)
                .transactionType(TransactionType.GOAL_CONTRIBUTION)
                .status(TransactionStatus.PENDING)
                .referenceId(UUID.randomUUID().toString())
                .metadata(metadata)
                .note(transferRequestDto.getNotes())
                .build();

        transaction = transactionRepository.save(transaction);

        // 2️⃣ Create ledger entries
        LedgerEntryModel debitEntry = LedgerEntryModel.builder()
                .transactionModel(transaction)
                .accountModel(senderMainAccount)
                .amount(transferAmount.negate())
                .entryType(EntryType.DEBIT)
                .build();

        LedgerEntryModel creditEntry = LedgerEntryModel.builder()
                .transactionModel(transaction)
                .accountModel(recipientPiggyAccount)
                .amount(transferAmount)
                .entryType(EntryType.CREDIT)
                .build();

        // Set bidirectional relationship
        transaction.setLedgerEntries(new ArrayList<>(List.of(debitEntry, creditEntry)));

        try {
            // 3️⃣ Update sender balances
            BigDecimal newSenderMainBalance = senderMainAccount.getBalance().subtract(transferAmount);
            /**
             * Update new Recipient Main account Balance
             */
            BigDecimal newRecipientMainBalance = recipientPiggyAccount.getBalance().add(transferAmount);

            /**
             * Debit sender main balance
             */
            senderMainAccount.setBalance(newSenderMainBalance);

            /**
             * Credit recipient main balance
             */
            recipientPiggyAccount.setBalance(newRecipientMainBalance);
            recipientPiggyAccount.getPiggyGoalModel().setCurrentBalance(newRecipientMainBalance);

            //4️⃣ Save Sender main account and recipient
            accountRepository.save(senderMainAccount);
            accountRepository.save(recipientPiggyAccount);

            //5️⃣ Save debit entry and credit entry
            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            // ✅ SUCCESS: Save transaction
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            applicationEventPublisher.publishEvent(
                    new ContributeTransferCompletedEvent(
                            this,
                            senderUser,
                            recipientPiggyAccount.getPiggyGoalModel(), /*Recipient account*/
                            transferAmount,
                            "Transfer to Piggy Goal: " + recipientPiggyAccount.getPiggyGoalModel().getName(),
                            transaction.getId(),
                            transaction.getCreatedAt(),
                            transaction.getNote()
                    )
            );

            // 6️⃣ Build response with all required fields
            return TransferContributeResponseDto.builder()
                    .transactionId(transaction.getId())
                    .senderAccountId(senderMainAccount.getId())
                    .senderAccountNumber(senderMainAccount.getAccountNumber())
                    .recipientAccountId(recipientPiggyAccount.getId())
                    .recipientAccountNumber(recipientPiggyAccount.getAccountNumber())
                    .amount(transferAmount)
                    .goalName(recipientPiggyAccount.getPiggyGoalModel().getName())
                    .goalOwner(recipientPiggyAccount.getUserModel().getName())
                    .description("Transfer Contribute to " + recipientPiggyAccount.getPiggyGoalModel().getName())
                    .newMainBalance(senderMainAccount.getBalance())
                    .transactionType(TransactionType.GOAL_CONTRIBUTION)
                    .status(recipientPiggyAccount.getPiggyGoalModel().getStatus())
                    .completedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // ❌ FAILURE - mark transaction as failed
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }

    /**
     * Break Piggy Transfer
     *
     * @param userId
     * @param transferRequestDto
     * @return
     */
    @Override
    @Transactional
    public TransferBreakPiggyResponseDto transferBreak(UUID userId, TransferBreakRequestDto transferRequestDto) {

        // 1. Find user
        UserModel userOwner = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException("User Account not found."));

        // 2. Validate PIN (implement your logic)
        // if (!pinService.validatePin(userId, transferRequestDto.getPin())) {
        //     throw new InvalidPinException("Invalid PIN");
        // }

        // 3. Find piggy account by account number
        AccountModel piggyAccount = accountRepository
                .findByAccountNumberAndUserModelId(transferRequestDto.getPiggyAccountNumber(), userOwner.getId())
                .orElseThrow(() -> new AccountNotFoundException("Piggy Account not found."));

        // 4. Get associated piggy goal and verify it's active
        PiggyGoalModel piggyGoal = piggyAccount.getPiggyGoalModel();
        if (piggyGoal == null) {
            throw new IllegalStateException("Account is not a piggy goal account");
        }
        if (piggyGoal.getStatus() != GoalStatus.ACTIVE) {
            throw new PiggyGoalNotActiveException();
        }

        // 5. Find main account
        AccountModel mainAccount = accountRepository
                .findAccountModelsByUserModelIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new AccountNotFoundException("Main account not found"));

        BigDecimal currentBalance = piggyAccount.getBalance();

        // 6. Fetch penalty rate
        BigDecimal penaltyRate = getPenaltyRate(); // e.g., 0.10 for 10%

        BigDecimal penaltyAmount = currentBalance
                .multiply(penaltyRate)
                .setScale(2, RoundingMode.HALF_UP);

        if (penaltyAmount.compareTo(currentBalance) > 0) {
            penaltyAmount = currentBalance;
        }

        BigDecimal amountToCredit = currentBalance.subtract(penaltyAmount);

        // 7. Create main transaction
        Map<String, Object> mainMetadata = new HashMap<>();
        mainMetadata.put("description", "Early break of piggy goal: " + piggyGoal.getName());
        mainMetadata.put("penaltyAmount", penaltyAmount);
        mainMetadata.put("penaltyRate", penaltyRate);

        TransactionModel mainTransaction = TransactionModel.builder()
                .initiatedByUserModel(userOwner)
                .transactionType(TransactionType.GOAL_WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .referenceId(UUID.randomUUID().toString())
                .metadata(mainMetadata)
                .build();
        mainTransaction = transactionRepository.save(mainTransaction);

        // 8. Create ledger entries (debit piggy, credit main)
        LedgerEntryModel debitPiggy = LedgerEntryModel.builder()
                .transactionModel(mainTransaction)
                .accountModel(piggyAccount)
                .amount(currentBalance.negate())
                .entryType(EntryType.DEBIT)
                .build();

        LedgerEntryModel creditMain = LedgerEntryModel.builder()
                .transactionModel(mainTransaction)
                .accountModel(mainAccount)
                .amount(amountToCredit)
                .entryType(EntryType.CREDIT)
                .build();

        mainTransaction.setLedgerEntries(new ArrayList<>(List.of(debitPiggy, creditMain)));

        try {
            // 9. Update balances
            mainAccount.setBalance(mainAccount.getBalance().add(amountToCredit));
            piggyAccount.setBalance(BigDecimal.ZERO);

            // 10. Update piggy goal status
            piggyGoal.setStatus(GoalStatus.BROKEN);
            piggyGoal.setBrokenAt(LocalDateTime.now());
            piggyGoal.setBrokenAt(mainTransaction.getCreatedAt());

            // 11. Save all
            accountRepository.save(mainAccount);
            accountRepository.save(piggyAccount);
            piggyGoalRepository.save(piggyGoal);

            ledgerEntryRepository.save(debitPiggy);
            ledgerEntryRepository.save(creditMain);

            mainTransaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(mainTransaction);

            // 12. Publish event
            // 12. Publish event with full data
            applicationEventPublisher.publishEvent(new PiggyBrokenCompleteEvent(
                    this,
                    userOwner,
                    piggyGoal,
                    penaltyAmount,
                    amountToCredit,
                    mainTransaction.getId(),
                    mainTransaction.getCreatedAt(),
                    mainAccount.getBalance(),   // newMainBalance after credit
                    currentBalance,             // originalBalance
                    penaltyRate                 // penaltyRate (fraction)
            ));

            // 13. Build response matching TransferBreakPiggyResponseDto
            return TransferBreakPiggyResponseDto.builder()
                    .transactionId(mainTransaction.getId())
                    .piggyGoalId(piggyGoal.getId())
                    .goalName(piggyGoal.getName())
                    .originalBalance(currentBalance)
                    .penaltyPercentage(penaltyRate)
                    .penaltyAmount(penaltyAmount)
                    .returnAmount(amountToCredit)
                    .newMainBalance(mainAccount.getBalance())
                    .wasEarlyBreak(true)
                    .completedAt(mainTransaction.getCreatedAt())
                    .build();

        } catch (Exception e) {
            mainTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(mainTransaction);
            throw e;
        }
    }

    @Override
    public BigDecimal getPenaltyRate() {
        return new BigDecimal("0.10");
    }


}
