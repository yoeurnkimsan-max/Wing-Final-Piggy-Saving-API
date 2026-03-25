package com.example.piggy_saving.services.scheduler;

import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.models.enums.GoalStatus;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.repository.PiggyGoalRepository;
import com.example.piggy_saving.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterestRateCalculateCronJob {

    private final PiggyGoalRepository piggyGoalRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    private static final int DAYS_IN_YEAR = 365;
    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(5000); // USD 5,000

    /**
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void calculateAndApplyInterest() {
        log.info("Starting daily interest calculation");

        // Fetch all active piggy goals with their associated accounts (eager load user)
        List<PiggyGoalModel> activeGoals = piggyGoalRepository.findByStatus(GoalStatus.ACTIVE);

        // Group by user to get each user's main account balance once
        Map<UserModel, List<PiggyGoalModel>> goalsByUser = activeGoals.stream()
                .collect(Collectors.groupingBy(PiggyGoalModel::getUserModel));

        for (Map.Entry<UserModel, List<PiggyGoalModel>> entry : goalsByUser.entrySet()) {
            UserModel user = entry.getKey();
            List<PiggyGoalModel> userGoals = entry.getValue();

            // Get user's main account balance
            AccountModel mainAccount = accountRepository
                    .findAccountModelsByUserModelIdAndAccountType(user.getId(), AccountType.MAIN)
                    .orElse(null);
            if (mainAccount == null) {
                log.warn("User {} has no main account, skipping interest calculation", user.getId());
                continue;
            }

            BigDecimal mainBalance = mainAccount.getBalance();
            double rate = getInterestRate(mainBalance);

            // Calculate daily interest rate
            BigDecimal dailyRate = BigDecimal.valueOf(rate / DAYS_IN_YEAR);

            for (PiggyGoalModel goal : userGoals) {
                AccountModel piggyAccount = goal.getAccountModel();
                if (piggyAccount == null) {
                    log.warn("Piggy goal {} has no associated account, skipping", goal.getId());
                    continue;
                }

                LocalDateTime lockExpiresAt = goal.getLockExpiresAt();
                if (lockExpiresAt != null && lockExpiresAt.isBefore(LocalDateTime.now())) {
                    // Lock already expired – no further interest
                    log.debug("Lock expired for goal {}, skipping interest", goal.getId());
                    continue;
                }

                BigDecimal balance = piggyAccount.getBalance();
                BigDecimal dailyInterest = balance.multiply(dailyRate)
                        .setScale(4, RoundingMode.HALF_UP);

                if (dailyInterest.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // no interest to add
                }

                // Apply interest to piggy account
                piggyAccount.setBalance(balance.add(dailyInterest));
                accountRepository.save(piggyAccount);

                // Record transaction
                transactionService.createInterestTransaction(piggyAccount, dailyInterest);

                log.debug("Added interest of {} to piggy account {} (rate: {}%, balance: {})",
                        dailyInterest, piggyAccount.getAccountNumber(), rate * 100, balance);
            }
        }

        log.info("Interest calculation completed");
    }

    private double getInterestRate(BigDecimal mainBalance) {
        // Compare mainBalance with threshold (assuming USD)
        if (mainBalance.compareTo(THRESHOLD) >= 0) {
            return 0.03; // 3%
        } else {
            return 0.001; // 0.10%
        }
    }
}