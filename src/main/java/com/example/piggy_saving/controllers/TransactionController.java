package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.TransactionHistoryResponseDto;
import com.example.piggy_saving.models.enums.TransactionType;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionHistoryService transactionHistoryService;

    /**
     * Get all user transactions (paginated)
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<TransactionHistoryResponseDto>>> getUserTransactionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = userDetails.getUserId();
        Page<TransactionHistoryResponseDto> result = transactionHistoryService.getUserTransactionHistory(userId, page, size);

        ApiResponse<Page<TransactionHistoryResponseDto>> response = ApiResponse.<Page<TransactionHistoryResponseDto>>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Transaction history retrieved")
                .timestamp(LocalDateTime.now())
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get user transactions filtered by type (paginated)
     */
    @GetMapping("/history/type")
    public ResponseEntity<ApiResponse<Page<TransactionHistoryResponseDto>>> getUserTransactionHistoryByType(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = userDetails.getUserId();
        Page<TransactionHistoryResponseDto> result = transactionHistoryService.getUserTransactionHistoryByType(userId, type, page, size);

        ApiResponse<Page<TransactionHistoryResponseDto>> response = ApiResponse.<Page<TransactionHistoryResponseDto>>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Transaction history retrieved by type")
                .timestamp(LocalDateTime.now())
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get user transactions within a date range (paginated)
     */
    @GetMapping("/history/date-range")
    public ResponseEntity<ApiResponse<Page<TransactionHistoryResponseDto>>> getUserTransactionHistoryByDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = userDetails.getUserId();
        Page<TransactionHistoryResponseDto> result = transactionHistoryService.getUserTransactionHistoryByDateRange(userId, startDate, endDate, page, size);

        ApiResponse<Page<TransactionHistoryResponseDto>> response = ApiResponse.<Page<TransactionHistoryResponseDto>>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Transaction history retrieved by date range")
                .timestamp(LocalDateTime.now())
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get the most recent transactions (limited)
     */
    @GetMapping("/history/recent")
    public ResponseEntity<ApiResponse<List<TransactionHistoryResponseDto>>> getRecentTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit) {

        UUID userId = userDetails.getUserId();
        List<TransactionHistoryResponseDto> result = transactionHistoryService.getRecentTransactions(userId, limit);

        ApiResponse<List<TransactionHistoryResponseDto>> response = ApiResponse.<List<TransactionHistoryResponseDto>>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Recent transactions retrieved")
                .timestamp(LocalDateTime.now())
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }
}