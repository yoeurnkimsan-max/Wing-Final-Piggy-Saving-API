package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.TransferBreakRequestDto;
import com.example.piggy_saving.dto.request.TransferContributeRequestDto;
import com.example.piggy_saving.dto.request.TransferP2PRequestDto;
import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.dto.response.*;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferService {
    TransferResponseDto transferToPiggy(UUID userId, TransferToPiggyRequestDto transferRequestDto);
    TransferP2PResponseDto transferP2P(UUID userId, TransferP2PRequestDto transferRequestDto);
    TransferContributeResponseDto transferContribute(UUID userId, TransferContributeRequestDto transferRequestDto);
    TransferBreakPiggyResponseDto transferBreak(UUID userId, TransferBreakRequestDto transferRequestDto);
    BigDecimal getPenaltyRate();
}
