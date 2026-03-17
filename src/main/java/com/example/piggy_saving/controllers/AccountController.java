package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.CreatePiggyGoalResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.AccountService;
import com.example.piggy_saving.services.PiggyAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AccountController.BASE_ROUTE)
@AllArgsConstructor
public class AccountController {

    public static final String BASE_ROUTE = "/api/v1/accounts";

    private final AccountService accountService;
    private final PiggyAccountService piggyAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getAccounts() {

        ApiResponse<List<AccountResponseDto>> response =
                accountService.getAllAccount();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getAccountById(@PathVariable UUID accountId) {

        ApiResponse<AccountResponseDto> response =
                accountService.getAccountById(accountId);


        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                accountService.getAccountByUserId(userDetails.getUserId())
        );
    }
    /**
     * List all Piggy account
     */

    @GetMapping("/my-accounts/{accountType}")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getMyAccountsByType(
            @PathVariable AccountType accountType,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        ApiResponse<List<AccountResponseDto>> accounts = accountService.getAllAccountByUserIdAndType(
                userDetails.getUserId(), accountType
        );

        return ResponseEntity.ok(
                accounts
        );
    }

    /**
     * Create Piggy account
     */
    @PostMapping("/piggy-account")
    public ResponseEntity<ApiResponse<CreatePiggyGoalResponseDto>> createPiggyAccount(
            @Valid @RequestBody CreatePiggyRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){

        return ResponseEntity.ok(piggyAccountService.createPiggyAccount(userDetails.getUserId(), requestDto));
    }

    /**
     * Get Detail Specific piggy-account by id
     */
    @GetMapping("/piggy-account/{piggy_id}")
    public ResponseEntity<ApiResponse<PiggyGoalResponseDto>> getPiggyAccountById(
            @PathVariable UUID piggy_id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        return piggyAccountService.getPiggyAccountById(userDetails.getUserId(), piggy_id);
    }

}