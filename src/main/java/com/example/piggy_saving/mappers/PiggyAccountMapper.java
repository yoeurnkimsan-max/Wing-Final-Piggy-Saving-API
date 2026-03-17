package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.response.CreatePiggyGoalResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;
import com.example.piggy_saving.models.PiggyGoalModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PiggyAccountMapper {

    CreatePiggyGoalResponseDto toCreatePiggyGoalResponseDto(PiggyGoalModel piggyGoalModel);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "targetAmount", source = "targetAmount")
    @Mapping(target = "lockPeriodDays", source = "lockPeriodDays")
    PiggyGoalModel toPiggyGoal(CreatePiggyRequestDto createPiggyRequestDto);

    PiggyGoalResponseDto toPiggyGoalResponse(PiggyGoalModel piggyGoalModel);
}