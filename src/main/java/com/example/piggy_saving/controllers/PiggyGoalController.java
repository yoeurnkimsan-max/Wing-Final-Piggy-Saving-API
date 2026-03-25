package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.request.PiggyAccountChangeIsPublic;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.PiggyAccountResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalDetailResponseDto;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.AccountService;
import com.example.piggy_saving.services.PiggyAccountService;
import com.example.piggy_saving.services.PiggyGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/piggy")
@RequiredArgsConstructor
public class PiggyGoalController {

    private final PiggyGoalService piggyGoalService;
    private final AccountService accountService;
    // GET /api/v1/piggy
    @GetMapping
    public ResponseEntity<ApiResponse<List<PiggyGoalDetailResponseDto>>> getAllPiggyGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        List<PiggyGoalDetailResponseDto> goals = piggyGoalService.getAllPiggyGoals(userDetails.getUserId());
        ApiResponse<List<PiggyGoalDetailResponseDto>> response = ApiResponse.<List<PiggyGoalDetailResponseDto>>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Piggy goals retrieved successfully")
                .timestamp(LocalDateTime.now())
                .data(goals)
                .build();
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/piggy/{goalId}
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<PiggyGoalDetailResponseDto>> getPiggyGoalById(@PathVariable UUID goalId) {
        PiggyGoalDetailResponseDto goal = piggyGoalService.getPiggyGoalById(goalId);
        ApiResponse<PiggyGoalDetailResponseDto> response = ApiResponse.<PiggyGoalDetailResponseDto>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Piggy goal retrieved successfully")
                .timestamp(LocalDateTime.now())
                .data(goal)
                .build();
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/piggy/by-account/{accountNumber}
    @GetMapping("/by-account/{accountNumber}")
    public ResponseEntity<ApiResponse<PiggyGoalDetailResponseDto>> getPiggyGoalByAccountNumber(@PathVariable String accountNumber) {
        PiggyGoalDetailResponseDto goal = piggyGoalService.getPiggyGoalDetailsByAccountNumber(accountNumber);
        ApiResponse<PiggyGoalDetailResponseDto> response = ApiResponse.<PiggyGoalDetailResponseDto>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Piggy goal retrieved successfully")
                .timestamp(LocalDateTime.now())
                .data(goal)
                .build();
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<ApiResponse<PiggyGoalDetailResponseDto>> createPiggyGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreatePiggyRequestDto request) {

        UUID userId = userDetails.getUserId();
        PiggyGoalDetailResponseDto createdGoal = piggyGoalService.createPiggyGoal(userId, request);

        ApiResponse<PiggyGoalDetailResponseDto> response = ApiResponse.<PiggyGoalDetailResponseDto>builder()
                .success(true)
                .statusCode(HttpStatus.CREATED.value())
                .message("Piggy goal created successfully")
                .timestamp(LocalDateTime.now())
                .data(createdGoal)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PatchMapping("/{account_number}")
    public ResponseEntity<ApiResponse<PiggyAccountResponseDto>> updatePiggyAccountIsPublic(
            @PathVariable String account_number,
            @Valid @RequestBody PiggyAccountChangeIsPublic requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        PiggyAccountResponseDto responseService = accountService.updatePiggyIsPublicByAccountNumberAndUserId(account_number, userDetails.getUserId(),requestDto);
        ApiResponse<PiggyAccountResponseDto> apiResponse = ApiResponse.<PiggyAccountResponseDto>builder()
                .message("PiggyAccount successfully updated!")
                .statusCode(200)
                .data(responseService)
                .statusMessage("OK")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(200).body(apiResponse);
    }
}