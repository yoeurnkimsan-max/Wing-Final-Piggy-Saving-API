package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.models.*;
import com.example.piggy_saving.models.enums.EntryType;
import com.example.piggy_saving.models.enums.TransactionStatus;
import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.repository.LedgerEntryRepository;
import com.example.piggy_saving.repository.TransactionRepository;
import com.example.piggy_saving.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Override
    @Transactional
    public TransactionModel createInterestTransaction(AccountModel account, BigDecimal amount) {
        // Build metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "interest");
        metadata.put("description", "Daily interest earned");

        // Create transaction
        TransactionModel transaction = TransactionModel.builder()
                .initiatedByUserModel(account.getUserModel())
                .transactionType(TransactionType.INTEREST)
                .status(TransactionStatus.COMPLETED)
                .referenceId(UUID.randomUUID().toString())
                .note("Interest credited")
                .metadata(metadata)
                .ledgerEntries(Collections.emptyList()) // will set after creating entries
                .build();

        // Save transaction first to get ID (if needed for ledger entries)
        TransactionModel savedTransaction = transactionRepository.save(transaction);

        // Create ledger entry for the interest credit
        LedgerEntryModel ledgerEntry = LedgerEntryModel.builder()
                .transactionModel(savedTransaction)
                .accountModel(account)
                .entryType(EntryType.CREDIT)
                .amount(amount)
                .build();

        LedgerEntryModel savedEntry = ledgerEntryRepository.save(ledgerEntry);

        // Update transaction with the ledger entry list
        savedTransaction.setLedgerEntries(Collections.singletonList(savedEntry));
        return transactionRepository.save(savedTransaction);
    }

    // other methods...
}