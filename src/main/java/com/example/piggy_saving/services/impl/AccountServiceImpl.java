package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.statusEnum.AccountStatus;
import com.example.piggy_saving.exception.UserNotFoundException;
import com.example.piggy_saving.mappers.AccountMapper;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.repository.UserRepository;
import com.example.piggy_saving.services.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;
    @Override
    public ApiResponse<List<AccountResponseDto>> getAllAccount() {

        List<AccountResponseDto> userData = accountMapper.toAccountUserData(accountRepository.findAll());

            ApiResponse<List<AccountResponseDto>> accountResponseDto= ApiResponse.<List<AccountResponseDto>>builder()
                    .message("success")
                    .success(true)
                    .data(userData)
                    .build();

        return accountResponseDto;
    }

    @Override
    @Transactional
    public ApiResponse<List<AccountResponseDto>> getAccountByUserId(UUID userId) {


        boolean isUserExist = userRepository.existsUserModelById(userId);

        if(!isUserExist){
            throw new UserNotFoundException("User not found");
        }

        List<AccountResponseDto> accountResponseDtoList = accountMapper.toAccountUserData(accountRepository.findAllByUserModelId(userId));

        ApiResponse<List<AccountResponseDto>> accountResponseDto= ApiResponse.<List<AccountResponseDto>>builder()
                .message("success")
                .success(true)
                .data(accountResponseDtoList)
                .build();
        return accountResponseDto;
    }

    @Override
    @Transactional
    public void createMainAccount(UserModel user) {
        AccountModel accountModel = AccountModel.builder()
                .accountType(AccountType.MAIN)
                .userModel(user)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build();
        accountRepository.save(accountModel);
    }
}
