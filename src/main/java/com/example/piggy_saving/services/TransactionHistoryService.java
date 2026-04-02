package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.response.TransactionHistoryResponseDto;
import com.example.piggy_saving.models.enums.TransactionType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionHistoryService {
    Page<TransactionHistoryResponseDto> getUserTransactionHistory(
            UUID userId, int page, int size);

    Page<TransactionHistoryResponseDto> getUserTransactionHistoryByType(
            UUID userId, TransactionType type, int page, int size);

    Page<TransactionHistoryResponseDto> getUserTransactionHistoryByDateRange(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate, int page, int size);

    List<TransactionHistoryResponseDto> getRecentTransactions(UUID userId, int limit);

    Page<TransactionHistoryResponseDto> getAllTransactionHistory(UUID userId, int page, int size);

}
