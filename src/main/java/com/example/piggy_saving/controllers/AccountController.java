package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AccountController.BASE_ROUTE)
@AllArgsConstructor
public class AccountController {

    public static final String BASE_ROUTE = "/api/v1/accounts";

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getAccounts() {

        ApiResponse<List<AccountResponseDto>> response =
                accountService.getAllAccount();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getAccountById(@PathVariable UUID accountId){


        return null;
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                accountService.getAccountByUserId(userDetails.getUserId())
        );
    }
}