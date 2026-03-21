package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.*;
import com.example.piggy_saving.dto.response.*;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferService {
    /**
     * OWN: Transfer to own piggy
     * @param userId
     * @param transferRequestDto
     * @return
     */
    TransferResponseDto transferToPiggy(UUID userId, TransferRequestDto transferRequestDto);
    TransferP2PResponseDto transferP2P(UUID userId, TransferRequestDto transferRequestDto);
    TransferContributeResponseDto transferContribute(UUID userId, TransferRequestDto transferRequestDto);
    TransferBreakPiggyResponseDto transferBreak(UUID userId, TransferBreakRequestDto transferRequestDto);
    BigDecimal getPenaltyRate();
}
