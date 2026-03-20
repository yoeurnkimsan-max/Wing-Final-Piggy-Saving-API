package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.response.PiggyGoalDetailedResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;

import java.util.List;
import java.util.UUID;

public interface GoalService {
    List<PiggyGoalDetailedResponseDto> findAllMyGoals(UUID userId);
    PiggyGoalResponseDto findGoalById(String id);
}
