package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.response.PiggyGoalDetailResponseDto;
import java.util.List;
import java.util.UUID;

public interface PiggyGoalService {

    /**
     * Get all piggy goals for the authenticated user.
     */
    List<PiggyGoalDetailResponseDto> getAllPiggyGoals(UUID userUUID);

    /**
     * Get a single piggy goal by its ID.
     */
    PiggyGoalDetailResponseDto getPiggyGoalDetails(UUID goalId);

    /**
     * Get a single piggy goal by its associated account number.
     */
    PiggyGoalDetailResponseDto getPiggyGoalDetailsByAccountNumber(String accountNumber);

    PiggyGoalDetailResponseDto getPiggyGoalById(UUID goalId);

    PiggyGoalDetailResponseDto createPiggyGoal(UUID userId, CreatePiggyRequestDto request);

}