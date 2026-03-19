package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.response.AccountResponseDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.CreatePiggyGoalResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;
import com.example.piggy_saving.exception.AccountNotFoundException;
import com.example.piggy_saving.exception.NotFoundExceptionHandler;
import com.example.piggy_saving.exception.UserNotFoundException;
import com.example.piggy_saving.mappers.AccountMapper;
import com.example.piggy_saving.mappers.PiggyAccountMapper;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.models.enums.GoalStatus;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.repository.PiggyGoalRepository;
import com.example.piggy_saving.repository.UserRepository;
import com.example.piggy_saving.services.PiggyAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PiggyAccountServiceImpl implements PiggyAccountService {

    private final PiggyAccountMapper piggyAccountMapper;
    private final PiggyGoalRepository piggyGoalRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public ApiResponse<CreatePiggyGoalResponseDto> createPiggyAccount(UUID userId, CreatePiggyRequestDto createPiggyRequestDto) {

        // 1️⃣ Find existing user
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = now.plusDays(createPiggyRequestDto.getLockPeriodDays());
        LocalDateTime lockExpiresAt = endAt; // same as end date

        // 2️⃣ Create PiggyGoal
        PiggyGoalModel piggyGoal = PiggyGoalModel.builder()
                .userModel(user)
                .name(createPiggyRequestDto.getName())
                .targetAmount(createPiggyRequestDto.getTargetAmount())
                .lockPeriodDays(createPiggyRequestDto.getLockPeriodDays())
                .startAt(now)
                .endAt(endAt)
                .lockedAt(now)
                .lockExpiresAt(lockExpiresAt)
                .status(GoalStatus.ACTIVE)
                .currentBalance(BigDecimal.ZERO)
                .build();

        // 3️⃣ Create linked PIGGY account
        AccountModel account = AccountModel.builder()
                .accountType(AccountType.PIGGY)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .piggyGoalModel(piggyGoal) // only set piggyGoalModel, not userModel
                .userModel(user)
                .build();

        // 4️⃣ Set bidirectional mapping
        piggyGoal.setAccountModel(account);

        // 5️⃣ Save PiggyGoal (will cascade account)
        piggyGoalRepository.save(piggyGoal);

        // 6️⃣ Map response
        CreatePiggyGoalResponseDto mappingPiggyModel =
                piggyAccountMapper.toCreatePiggyGoalResponseDto(piggyGoal);

        // 7️⃣ Build API response
        return ApiResponse.<CreatePiggyGoalResponseDto>builder()
                .message("Create PiggyGoal successfully")
                .statusCode(201)
                .statusMessage("CREATED")
                .success(true)
                .data(mappingPiggyModel)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ResponseEntity<ApiResponse<PiggyGoalResponseDto>> getPiggyAccountByPiggyAccountNumber(UUID userId, String piggyAccountNumber) {

        return null;

//        AccountModel piggyAccount = accountRepository.findByAccountNumberAndUserModelId(piggyAccountNumber, userId).orElseThrow(() -> new AccountNotFoundException("Account not found"));
//
//
//
//        PiggyGoalModel piggyGoalModel = piggyGoalRepository.findByIdAndUserModelId(piggyId, userId)
//                .orElseThrow(() -> new AccountNotFoundException("Piggy Goal not found"));
//
//        if (piggyGoalModel == null) {
//            throw new NotFoundExceptionHandler("Piggy goal not found");
//        }
//
//        PiggyGoalResponseDto toDto = piggyAccountMapper.toPiggyGoalResponse(piggyGoalModel);
//
//        ApiResponse<PiggyGoalResponseDto> dataResponse = ApiResponse.<PiggyGoalResponseDto>builder()
//                .success(true)
//                .message("Get PiggyGoal successfully")
//                .statusCode(200)
//                .statusMessage("SUCCESS")
//                .data(toDto)
//                .build();
//
//        return ResponseEntity.ok(dataResponse);
    }
}