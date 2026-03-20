package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.response.PiggyGoalDetailedResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;
import com.example.piggy_saving.mappers.PiggyGoalMapper;
import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.repository.PiggyGoalRepository;
import com.example.piggy_saving.services.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final PiggyGoalRepository piggyGoalRepository;
    private final PiggyGoalMapper piggyGoalMapper;


    @Override
    public List<PiggyGoalDetailedResponseDto> findAllMyGoals(UUID userId) {

        List<PiggyGoalModel> piggyGoalModels = piggyGoalRepository.findByUserModelId(userId);

        List<PiggyGoalDetailedResponseDto> piggyGoalResponseDtoList = piggyGoalMapper.toPiggyGoalResponseDtoList(piggyGoalModels);

        return piggyGoalResponseDtoList;
    }

    @Override
    public PiggyGoalResponseDto findGoalById(String id) {
        return null;
    }
}
