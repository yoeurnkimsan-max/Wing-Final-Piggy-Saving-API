package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.response.PiggyGoalDetailedResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;
import com.example.piggy_saving.models.PiggyGoalModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PiggyGoalMapper {
    @Mapping(source = "userModel.id", target = "userId")
    @Mapping(source = "currentBalance", target = "amount")
    @Mapping(target = "hideBalance", expression = "java(false)") // or calculate
    List<PiggyGoalDetailedResponseDto> toPiggyGoalResponseDtoList(List<PiggyGoalModel> piggyGoalModels);
}
