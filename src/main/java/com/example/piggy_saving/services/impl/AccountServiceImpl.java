package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.PiggyAccountChangeIsPublic;
import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.PiggyAccountResponseDto;
import com.example.piggy_saving.dto.response.SummarizeAccountResponseDto;
import com.example.piggy_saving.exception.AccountNotFoundException;
import com.example.piggy_saving.exception.UserNotFoundException;
import com.example.piggy_saving.mappers.AccountMapper;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.repository.UserRepository;
import com.example.piggy_saving.services.AccountService;
import com.example.piggy_saving.util.AccountNumberGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        List<AccountResponseDto> userData = accountMapper.toAccountUserDataAsList(accountRepository.findAll());

        ApiResponse<List<AccountResponseDto>> accountResponseDto = ApiResponse.<List<AccountResponseDto>>builder()
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

        if (!isUserExist) {
            throw new UserNotFoundException("User not found");
        }

        List<AccountResponseDto> accountResponseDtoList = accountMapper.toAccountUserDataAsList(accountRepository.findAllAccountsByUserId(userId));

        ApiResponse<List<AccountResponseDto>> accountResponseDto = ApiResponse.<List<AccountResponseDto>>builder()
                .message("SUCCESS")
                .success(true)
                .data(accountResponseDtoList)
                .build();
        return accountResponseDto;
    }

    @Override
    @Transactional
    public ApiResponse<AccountResponseDto> getAccountByAccountNumberAndAccountType(String accountNumber, AccountType accountType) {

        AccountModel account = accountRepository.findByAccountNumberAndAccountType(accountNumber, accountType)
                .orElseThrow(() -> new AccountNotFoundException("Account with Account number: " + accountNumber + " not found"));

        if (!account.isPublic()) {
            throw new AccountNotFoundException("Account with Account number: " + accountNumber + " id private.");
        }

//        AccountResponseDto accountDataResponseMapper = accountMapper.toAccountUserData(account);

        AccountResponseDto accountResponseDto = AccountResponseDto.builder()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .createdAt(account.getCreatedAt())
                .currency(account.getCurrency())
                .id(account.getId())
                .isPublic(account.isPublic())
                .balance(account.getBalance())
                .userId(account.getUserModel().getId())
                .build();

        ApiResponse<AccountResponseDto> accountDataApiResponse = ApiResponse.<AccountResponseDto>builder()
                .message("SUCCESS")
                .success(true)
                .data(accountResponseDto)
                .timestamp(LocalDateTime.now())
                .build();

        return accountDataApiResponse;
    }

    @Override
    @Transactional
    public void createMainAccount(UserModel user) {
        AccountModel accountModel = AccountModel.builder()
                .accountType(AccountType.MAIN)
                .userModel(user)
                .balance(BigDecimal.ZERO)
                .isPublic(true)
                .accountNumber(AccountNumberGenerator.generateAccountNumber())
                .currency("USD")
                .build();
        accountRepository.save(accountModel);
    }

    @Override
    public ApiResponse<List<AccountResponseDto>> getAllAccountByUserIdAndType(UUID userId, AccountType accountType) {


        List<AccountResponseDto> accountResponseDtoList = accountMapper.toAccountUserDataAsList(accountRepository.findAllByUserModelIdAndAccountType(userId, accountType));


        ApiResponse<List<AccountResponseDto>> listApiResponse = ApiResponse.<List<AccountResponseDto>>builder()
                .data(accountResponseDtoList)
                .success(true)
                .timestamp(LocalDateTime.now())
                .statusMessage("success")
                .statusCode(200)
                .message("success")
                .build();

        return listApiResponse;
    }

    @Override
    public List<PiggyAccountResponseDto> getPiggyAccountByUserId(UUID userId) {
//        Account
        List<AccountModel> accountModelList = accountRepository.findPiggyAccountsByUserId(userId);
        return accountMapper.toPiggyAccountResponseListDto(accountModelList);
    }

    @Override
    public PiggyAccountResponseDto updatePiggyIsPublicByAccountNumberAndUserId(String accountNumber, UUID userId, PiggyAccountChangeIsPublic piggyAccountChangeIsPublic) {


        AccountModel getAccountUpdate = accountRepository.findByAccountNumberAndUserModelId(accountNumber, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found or fetch failed"));

        getAccountUpdate.setPublic(piggyAccountChangeIsPublic.isChangePiggyToIsPublic());

        accountRepository.save(getAccountUpdate);

        PiggyAccountResponseDto piggyAccountResponseDto = accountMapper.toPiggyAccountResponseDto(getAccountUpdate);

        return piggyAccountResponseDto;
    }

    @Override
    public AccountResponseDto getMainAccountByUserId(UUID userId) {

        AccountModel accountMain = accountRepository.getByUserModelIdAndAccountTypeIs(userId, AccountType.MAIN).orElseThrow(
                () -> new AccountNotFoundException("Account not found or fetch failed")
        );

        AccountResponseDto accountResponseDto = AccountResponseDto.builder()
                .userId(accountMain.getUserModel().getId())
                .accountType(accountMain.getAccountType())
                .createdAt(accountMain.getCreatedAt())
                .currency(accountMain.getCurrency())
                .id(accountMain.getId())
                .isPublic(accountMain.isPublic())
                .balance(accountMain.getBalance())
                .username(accountMain.getUserModel().getName())
                .accountNumber(accountMain.getAccountNumber())
                .build();

        return accountResponseDto;
    }

    @Override
    public SummarizeAccountResponseDto summarizeAccountByUserId(UUID userId) {

        return null;
    }
}
