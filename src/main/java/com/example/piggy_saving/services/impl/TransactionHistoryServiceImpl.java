package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.response.TransactionHistoryResponseDto;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.LedgerEntryModel;
import com.example.piggy_saving.models.TransactionModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.enums.EntryType;
import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.repository.TransactionRepository;
import com.example.piggy_saving.services.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public Page<TransactionHistoryResponseDto> getUserTransactionHistory(
            UUID userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionModel> transactions = transactionRepository
                .findTransactionsByUserId(userId, pageable);

        return transactions.map(transaction -> mapToDto(transaction, userId));
    }

    @Override
    public Page<TransactionHistoryResponseDto> getUserTransactionHistoryByType(
            UUID userId, TransactionType type, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionModel> transactions = transactionRepository
                .findTransactionsByUserIdAndType(userId, type, pageable);

        return transactions.map(transaction -> mapToDto(transaction, userId));
    }

    @Override
    public Page<TransactionHistoryResponseDto> getUserTransactionHistoryByDateRange(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionModel> transactions = transactionRepository
                .findTransactionsByUserIdAndDateRange(userId, startDate, endDate, pageable);

        return transactions.map(transaction -> mapToDto(transaction, userId));
    }

    @Override
    public List<TransactionHistoryResponseDto> getRecentTransactions(UUID userId, int limit) {
        List<TransactionModel> transactions = transactionRepository
                .findTop10ByInitiatedByUserModelIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

        return transactions.stream()
                .map(transaction -> mapToDto(transaction, userId))
                .collect(Collectors.toList());
    }

    private TransactionHistoryResponseDto mapToDto(TransactionModel transaction, UUID userId) {
        // Determine which ledger entry belongs to the current user
        LedgerEntryModel userEntry = transaction.getLedgerEntries().stream()
                .filter(entry -> entry.getAccountModel().getUserModel().getId().equals(userId))
                .findFirst()
                .orElse(null);

        // Find counterparty entry
        LedgerEntryModel counterpartyEntry = transaction.getLedgerEntries().stream()
                .filter(entry -> !entry.getAccountModel().getUserModel().getId().equals(userId))
                .findFirst()
                .orElse(null);

        // Determine counterparty details
        String counterpartyName = "";
        String counterpartyEmail = "";
        String goalName = "";

        if (counterpartyEntry != null) {
            AccountModel counterpartyAccount = counterpartyEntry.getAccountModel();
            UserModel counterpartyUser = counterpartyAccount.getUserModel();

            if (counterpartyAccount.getPiggyGoalModel() != null) {
                goalName = counterpartyAccount.getPiggyGoalModel().getName();
                counterpartyName = counterpartyUser.getName();
            } else {
                counterpartyName = counterpartyUser.getName();
                counterpartyEmail = counterpartyUser.getEmail();
            }
        }

        // Determine from/to account masks
        String fromAccountMask = "";
        String toAccountMask = "";

        for (LedgerEntryModel entry : transaction.getLedgerEntries()) {
            if (entry.getEntryType() == EntryType.DEBIT) {
                fromAccountMask = maskAccountNumber(entry.getAccountModel().getAccountNumber());
            } else if (entry.getEntryType() == EntryType.CREDIT) {
                toAccountMask = maskAccountNumber(entry.getAccountModel().getAccountNumber());
            }
        }

        return TransactionHistoryResponseDto.builder()
                .transactionId(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .amount(userEntry != null ? userEntry.getAmount().abs() : BigDecimal.ZERO)
                .counterpartyName(counterpartyName)
                .counterpartyEmail(counterpartyEmail)
                .goalName(goalName)
                .createdAt(transaction.getCreatedAt())
                .fromAccountMask(fromAccountMask)
                .toAccountMask(toAccountMask)
                .entryType(userEntry != null ? userEntry.getEntryType() : null)
                .balanceAfter(userEntry != null ? userEntry.getAccountModel().getBalance() : null)
                .metadata(transaction.getMetadata())   // <-- add this line
                .build();
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return "••••";
        return "•••• " + accountNumber.substring(accountNumber.length() - 4);
    }
}
