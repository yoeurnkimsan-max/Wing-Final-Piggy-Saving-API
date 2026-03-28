package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.request.PiggyAccountChangeIsPublic;
import com.example.piggy_saving.dto.response.*;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.AccountService;
import com.example.piggy_saving.services.PiggyAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @GetMapping("/main")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getMainAccount(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        AccountResponseDto serviceResponse = accountService.getMainAccountByUserId(customUserDetails.getUserId());

        ApiResponse<AccountResponseDto> apiResponse = ApiResponse.<AccountResponseDto>builder()
                .success(true)
                .statusCode(200)
                .message("Main account successfully fetched")
                .statusMessage("OK")
                .timestamp(LocalDateTime.now())
                .data(serviceResponse)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getAccountById(
            @PathVariable String accountNumber,
            @RequestParam(required = true) AccountType type
    ) {

        ApiResponse<AccountResponseDto> response =
                accountService.getAccountByAccountNumberAndAccountType(accountNumber, type);


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


    @GetMapping("/piggy-account")
    public ResponseEntity<ApiResponse<List<PiggyAccountResponseDto>>> getPiggyAccountById(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<PiggyAccountResponseDto> serviceResponse= accountService.getPiggyAccountByUserId(userDetails.getUserId());
        ApiResponse<List<PiggyAccountResponseDto>> apiResponse = ApiResponse.<List<PiggyAccountResponseDto>>builder()
                .message("PiggyAccount successfully fetched!")
                .statusCode(200)
                .data(serviceResponse)
                .statusMessage("OK")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
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

    /**
     * Get Detail Specific piggy-account by piggy-account number
     */
    @GetMapping("/piggy-account/{piggy_account_number}")
    public ResponseEntity<ApiResponse<PiggyGoalResponseDto>> getPiggyAccountById(
            @PathVariable String piggy_account_number,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return piggyAccountService.getPiggyAccountByPiggyAccountNumber(userDetails.getUserId(), piggy_account_number);
    }


}