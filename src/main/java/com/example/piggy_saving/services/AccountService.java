package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.models.UserModel;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    ApiResponse<List<AccountResponseDto>> getAllAccount();
    ApiResponse<List<AccountResponseDto>> getAccountByUserId(UUID userId);
    public void createMainAccount(UserModel user);
}
