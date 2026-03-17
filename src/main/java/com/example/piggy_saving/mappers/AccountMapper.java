package com.example.piggy_saving.mappers;


import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.models.AccountModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {
    List<AccountResponseDto> toAccountUserDataAsList(List<AccountModel> accountModel);
    AccountResponseDto toAccountUserData(AccountModel accountModel);
}
