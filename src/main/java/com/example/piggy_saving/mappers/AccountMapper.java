package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.models.AccountModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

//    @Mapping(target = "account_number", source = "accountNumber")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userModel.id")
    @Mapping(target = "accountType", source = "accountType") // Map enum to string automatically
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "piggyGoalId", source = "piggyGoalModel.id")
    @Mapping(target = "createdAt", source = "createdAt")
//    @Mapping(target = "updatedAt", source = "updatedAt")
    AccountResponseDto toAccountUserData(AccountModel accountModel);

    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "isPublic", source = "isPublic")
    List<AccountResponseDto> toAccountUserDataAsList(List<AccountModel> accountModels);
}