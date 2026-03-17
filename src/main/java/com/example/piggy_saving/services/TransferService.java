package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.dto.response.TransferResponseDto;

import java.util.UUID;

public interface TransferService {
    TransferResponseDto transferToPiggy(UUID userId, TransferToPiggyRequestDto transferRequestDto);
}
