package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.CreatePiggyRequestDto;
import com.example.piggy_saving.dto.response.PiggyGoalDetailResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalDetailedResponseDto;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.models.enums.GoalStatus;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.repository.PiggyGoalRepository;
import com.example.piggy_saving.repository.UserRepository;
import com.example.piggy_saving.services.PiggyGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PiggyGoalServiceImpl implements PiggyGoalService {

    private final PiggyGoalRepository piggyGoalRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PiggyGoalDetailResponseDto> getAllPiggyGoals(UUID userUUID) {
        return piggyGoalRepository.findByUserModelId(userUUID).stream()
                .map(goal -> mapToDto(goal, goal.getAccountModel()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PiggyGoalDetailResponseDto> getAllPiggyGoalsByUserIdAndStatus(UUID userUUID, GoalStatus status) {
        return piggyGoalRepository.findByUserModelIdAndStatus(userUUID, status).stream()
                .map(goal -> mapToDto(goal, goal.getAccountModel()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PiggyGoalDetailResponseDto getPiggyGoalDetails(UUID goalId) {
        PiggyGoalModel goal = piggyGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Piggy goal not found"));
        AccountModel account = goal.getAccountModel();
        if (account == null) {
            throw new RuntimeException("Account not found for this piggy goal");
        }
        return mapToDto(goal, account);
    }

    @Override
    @Transactional(readOnly = true)
    public PiggyGoalDetailResponseDto getPiggyGoalDetailsByAccountNumber(String accountNumber) {
        AccountModel account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        PiggyGoalModel goal = account.getPiggyGoalModel();
        if (goal == null) {
            throw new RuntimeException("No piggy goal associated with this account");
        }
        return mapToDto(goal, account);
    }

    @Override
    @Transactional(readOnly = true)
    public PiggyGoalDetailResponseDto getPiggyGoalById(UUID goalId) {
        // Reuse the existing method
        return getPiggyGoalDetails(goalId);
    }

    @Override
    @Transactional
    public PiggyGoalDetailResponseDto createPiggyGoal(UUID userId, CreatePiggyRequestDto request) {
        // 1. Find the user (you may inject UserRepository)
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Create the piggy goal
        PiggyGoalModel piggyGoal = PiggyGoalModel.builder()
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .status(GoalStatus.ACTIVE) // initial status
                .currentBalance(BigDecimal.ZERO)
                .lockExpiresAt(LocalDateTime.now().plusDays(request.getLockPeriodDays()))
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(request.getLockPeriodDays()))
                .lockPeriodDays(request.getLockPeriodDays())
                .lockedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .userModel(user)
                .build();

        // 4. Create the associated account
        AccountModel account = AccountModel.builder()
                .userModel(user)
                .accountType(AccountType.PIGGY)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .isPublic(false)                 // default private (can be changed later)
                .isHideBalance(request.isHideBalance())
                .piggyGoalModel(piggyGoal)       // link to piggy goal
                .build();

        // 5. Set the account on the piggy goal (bidirectional)
        piggyGoal.setAccountModel(account);

        // 6. Save both (cascade should handle if configured)
        piggyGoalRepository.save(piggyGoal);
        accountRepository.save(account);

        // 7. Return the DTO
        return mapToDto(piggyGoal, account);
    }

    private PiggyGoalDetailResponseDto mapToDto(PiggyGoalModel goal, AccountModel account) {
        return PiggyGoalDetailResponseDto.builder()
                .id(goal.getId().toString())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount())
                .status(goal.getStatus())
                .lockExpiresAt(goal.getLockExpiresAt())
                .createdAt(goal.getCreatedAt())
                .accountNumber(account.getAccountNumber())
                .currentBalance(account.getBalance())
                .isPublic(account.isPublic())
                .currency(account.getCurrency())
                .build();
    }
}