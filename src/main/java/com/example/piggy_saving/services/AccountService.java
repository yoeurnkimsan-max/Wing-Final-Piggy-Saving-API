package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.PiggyAccountChangeIsPublic;
import com.example.piggy_saving.dto.response.*;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.enums.AccountType;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    ApiResponse<List<AccountResponseDto>> getAllAccount();
    ApiResponse<List<AccountResponseDto>> getAccountByUserId(UUID userId);
    ApiResponse<AccountResponseDto> getAccountByAccountNumberAndAccountType(String accountNumber, AccountType accountType);
    void createMainAccount(UserModel user);
    ApiResponse<List<AccountResponseDto>> getAllAccountByUserIdAndType(UUID userId,AccountType accountType);

    List<PiggyAccountResponseDto> getPiggyAccountByUserId(UUID userId);
    PiggyAccountResponseDto updatePiggyIsPublicByAccountNumberAndUserId(String accountNumber, UUID userId, PiggyAccountChangeIsPublic piggyAccountChangeIsPublic);

    AccountResponseDto getMainAccountByUserId(UUID userId);

    SummarizeAccountResponseDto summarizeAccountByUserId(UUID userId);
}
