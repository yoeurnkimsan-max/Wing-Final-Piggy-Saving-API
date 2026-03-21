package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.TransactionModel;
import com.example.piggy_saving.models.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionModel, UUID> {

    @Query("SELECT t FROM TransactionModel t WHERE t.initiatedByUserModel.id = :userId")
    List<TransactionModel> findAllByInitiatedByUserModelId(@Param("userId") UUID userId);


    // Get all transactions for a user (both sent and received)
    @Query("SELECT DISTINCT t FROM TransactionModel t " +
            "LEFT JOIN FETCH t.ledgerEntries l " +
            "WHERE t.initiatedByUserModel.id = :userId " +
            "OR l.accountModel.userModel.id = :userId " +
            "ORDER BY t.createdAt DESC")
    Page<TransactionModel> findTransactionsByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Get transactions by type (P2P, CONTRIBUTION, BREAK, etc.)
    @Query("SELECT DISTINCT t FROM TransactionModel t " +
            "LEFT JOIN FETCH t.ledgerEntries l " +
            "WHERE (t.initiatedByUserModel.id = :userId OR l.accountModel.userModel.id = :userId) " +
            "AND t.transactionType = :transactionType " +
            "ORDER BY t.createdAt DESC")
    Page<TransactionModel> findTransactionsByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable);

    // Get transactions within a date range
    @Query("SELECT DISTINCT t FROM TransactionModel t " +
            "LEFT JOIN FETCH t.ledgerEntries l " +
            "WHERE (t.initiatedByUserModel.id = :userId OR l.accountModel.userModel.id = :userId) " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    Page<TransactionModel> findTransactionsByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Get recent transactions (limit)
    List<TransactionModel> findTop10ByInitiatedByUserModelIdOrderByCreatedAtDesc(UUID userId);
}
