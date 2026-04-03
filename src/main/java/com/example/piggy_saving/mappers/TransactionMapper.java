package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.response.TransactionHistoryResponseDto;
import com.example.piggy_saving.models.LedgerEntryModel;
import com.example.piggy_saving.models.TransactionModel;
import com.example.piggy_saving.models.enums.EntryType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "transactionType", source = "transactionType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "description", source = "note")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "amount", source = "ledgerEntries", qualifiedByName = "extractAmount")
    @Mapping(target = "counterpartyName", source = "ledgerEntries", qualifiedByName = "extractCounterpartyName")
    @Mapping(target = "counterpartyEmail", source = "ledgerEntries", qualifiedByName = "extractCounterpartyEmail")
    @Mapping(target = "goalName", source = "ledgerEntries", qualifiedByName = "extractGoalName")
    @Mapping(target = "fromAccountMask", source = "ledgerEntries", qualifiedByName = "extractFromAccountMask")
    @Mapping(target = "toAccountMask", source = "ledgerEntries", qualifiedByName = "extractToAccountMask")
    @Mapping(target = "entryType", ignore = true) // Not needed for admin view
    @Mapping(target = "balanceAfter", ignore = true) // Not needed for admin view
    TransactionHistoryResponseDto toAdminDto(TransactionModel transaction);

    @Named("extractAmount")
    default BigDecimal extractAmount(java.util.List<LedgerEntryModel> ledgerEntries) {
        if (ledgerEntries == null || ledgerEntries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return ledgerEntries.get(0).getAmount().abs();
    }

    @Named("extractCounterpartyName")
    default String extractCounterpartyName(java.util.List<LedgerEntryModel> ledgerEntries) {
        if (ledgerEntries == null || ledgerEntries.size() < 2) {
            return "N/A";
        }

        // Find the credit entry (receiver) for counterparty name
        LedgerEntryModel creditEntry = ledgerEntries.stream()
                .filter(entry -> entry.getEntryType() == EntryType.CREDIT)
                .findFirst()
                .orElse(null);

        if (creditEntry != null && creditEntry.getAccountModel() != null
                && creditEntry.getAccountModel().getUserModel() != null) {
            return creditEntry.getAccountModel().getUserModel().getName();
        }

        // If no credit entry, get from debit entry
        LedgerEntryModel debitEntry = ledgerEntries.stream()
                .filter(entry -> entry.getEntryType() == EntryType.DEBIT)
                .findFirst()
                .orElse(null);

        if (debitEntry != null && debitEntry.getAccountModel() != null
                && debitEntry.getAccountModel().getUserModel() != null) {
            return debitEntry.getAccountModel().getUserModel().getName();
        }

        return "System";
    }

    @Named("extractCounterpartyEmail")
    default String extractCounterpartyEmail(java.util.List<LedgerEntryModel> ledgerEntries) {
        if (ledgerEntries == null || ledgerEntries.size() < 2) {
            return null;
        }

        LedgerEntryModel creditEntry = ledgerEntries.stream()
                .filter(entry -> entry.getEntryType() == EntryType.CREDIT)
                .findFirst()
                .orElse(null);

        if (creditEntry != null && creditEntry.getAccountModel() != null
                && creditEntry.getAccountModel().getUserModel() != null) {
            return creditEntry.getAccountModel().getUserModel().getEmail();
        }

        return null;
    }

    @Named("extractGoalName")
    default String extractGoalName(java.util.List<LedgerEntryModel> ledgerEntries) {
        if (ledgerEntries == null) {
            return null;
        }

        // Check if any account is associated with a piggy goal
        for (LedgerEntryModel entry : ledgerEntries) {
            if (entry.getAccountModel() != null
                    && entry.getAccountModel().getPiggyGoalModel() != null) {
                return entry.getAccountModel().getPiggyGoalModel().getName();
            }
        }

        return null;
    }

    @Named("extractFromAccountMask")
    default String extractFromAccountMask(java.util.List<LedgerEntryModel> ledgerEntries) {
        if (ledgerEntries == null) {
            return null;
        }

        LedgerEntryModel debitEntry = ledgerEntries.stream()
                .filter(entry -> entry.getEntryType() == EntryType.DEBIT)
                .findFirst()
                .orElse(null);

        if (debitEntry != null && debitEntry.getAccountModel() != null) {
            return maskAccountNumber(debitEntry.getAccountModel().getAccountNumber());
        }

        return null;
    }

    @Named("extractToAccountMask")
    default String extractToAccountMask(java.util.List<LedgerEntryModel> ledgerEntries) {
        if (ledgerEntries == null) {
            return null;
        }

        LedgerEntryModel creditEntry = ledgerEntries.stream()
                .filter(entry -> entry.getEntryType() == EntryType.CREDIT)
                .findFirst()
                .orElse(null);

        if (creditEntry != null && creditEntry.getAccountModel() != null) {
            return maskAccountNumber(creditEntry.getAccountModel().getAccountNumber());
        }

        return null;
    }

    @Named("maskAccount")
    default String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "••••";
        }
        return "•••• " + accountNumber.substring(accountNumber.length() - 4);
    }
}