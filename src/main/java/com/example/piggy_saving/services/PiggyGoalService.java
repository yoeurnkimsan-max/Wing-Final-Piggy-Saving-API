package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.response.PiggyGoalDetailResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalDetailedResponseDto;
import com.example.piggy_saving.models.enums.GoalStatus;

import java.util.List;
import java.util.UUID;

public interface PiggyGoalService {

    /**
     * Get all piggy goals for the authenticated user.
     */
    List<PiggyGoalDetailResponseDto> getAllPiggyGoals(UUID userUUID);

    List<PiggyGoalDetailResponseDto> getAllPiggyGoalsByUserIdAndStatus(UUID userUUID, GoalStatus status);

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