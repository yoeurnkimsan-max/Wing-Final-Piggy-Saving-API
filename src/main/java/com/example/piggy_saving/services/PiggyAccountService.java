package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.CreatePiggyGoalResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface PiggyAccountService {
    ApiResponse<CreatePiggyGoalResponseDto> createPiggyAccount(UUID userId, CreatePiggyRequestDto createPiggyRequestDto);
    ResponseEntity<ApiResponse<PiggyGoalResponseDto>> getPiggyAccountByPiggyAccountNumber(UUID userId, String piggyAccountNumber);
}
