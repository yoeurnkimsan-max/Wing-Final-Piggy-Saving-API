package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.response.RegisterResponseDto;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthMapper {
    @Mapping(source = "id", target = "userId")
    RegisterResponseDto toRegisterResponseDto(UserModel userModel);
}
