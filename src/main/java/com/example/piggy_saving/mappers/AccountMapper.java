package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.PiggyAccountResponseDto;
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
//    @Mapping(target = "isPublic", source = "is_public")
//    @Mapping(target = "updatedAt", source = "updatedAt")
    AccountResponseDto toAccountUserData(AccountModel accountModel);

    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "isPublic", source = "is_public")
    List<AccountResponseDto> toAccountUserDataAsList(List<AccountModel> accountModels);



    /**
     * Fetch Piggy account
     * @param accountModel
     * @return
     */
    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "userModel.id", target = "userId")
    @Mapping(source = "piggyGoalModel.id", target = "piggyGoalId")
    @Mapping(source = "piggyGoalModel.name", target = "goalName")
    @Mapping(source = "piggyGoalModel.status", target = "goalStatus")
    @Mapping(source = "piggyGoalModel.currentBalance", target = "currentBalance")
    @Mapping(source = "piggyGoalModel.targetAmount", target = "targetAmount")
    @Mapping(source = "piggyGoalModel.lockedAt", target = "lockedAt")
    @Mapping(source = "piggyGoalModel.lockExpiresAt", target = "lockExpiresAt")
    @Mapping(source = "public", target = "isPublic")
    PiggyAccountResponseDto toPiggyAccountResponseDto(AccountModel accountModel);

    List<PiggyAccountResponseDto> toPiggyAccountResponseListDto(List<AccountModel> accountModelList);
//    PiggyAccountResponseDto toPiggyAccountResponseDto(AccountModel accountModel);
}