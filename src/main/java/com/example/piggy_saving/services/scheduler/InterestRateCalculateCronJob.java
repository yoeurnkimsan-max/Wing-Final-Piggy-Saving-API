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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    private final PlatformTransactionManager transactionManager;

    private static final int DAYS_IN_YEAR = 365;

    /**
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateAndApplyInterest() {
        log.info("Starting daily interest calculation");

        List<PiggyGoalModel> activeGoals = piggyGoalRepository.findByStatus(GoalStatus.ACTIVE);
        if (activeGoals.isEmpty()) {
            log.info("No active piggy goals found. Exiting.");
            return;
        }

        Map<UserModel, List<PiggyGoalModel>> goalsByUser = activeGoals.stream()
                .collect(Collectors.groupingBy(PiggyGoalModel::getUserModel));

        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<UserModel, List<PiggyGoalModel>> entry : goalsByUser.entrySet()) {
            UserModel user = entry.getKey();
            List<PiggyGoalModel> userGoals = entry.getValue();

            try {
                processUserInterest(user, userGoals);
                successCount++;
                log.info("Successfully processed interest for user: {}", user.getId());
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to process interest for user: {}. Error: {}", user.getId(), e.getMessage(), e);
            }
        }

        log.info("Interest calculation completed. Success: {}, Failed: {}", successCount, failureCount);
    }

    private void processUserInterest(UserModel user, List<PiggyGoalModel> userGoals) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute((TransactionCallback<Void>) status -> {
            try {
                for (PiggyGoalModel goal : userGoals) {
                    // Skip if goal has matured
                    if (goal.getLockExpiresAt() != null && LocalDateTime.now().isAfter(goal.getLockExpiresAt())) {
                        handleMaturity(goal);
                        continue;
                    }

                    // Prevent double calculation
                    if (goal.getLastInterestCalculatedAt() != null &&
                            goal.getLastInterestCalculatedAt().toLocalDate().equals(LocalDate.now())) {
                        continue;
                    }

                    AccountModel piggyAccount = goal.getAccountModel();
                    if (piggyAccount == null) {
                        log.warn("Piggy goal {} has no associated account, skipping", goal.getId());
                        continue;
                    }

                    BigDecimal balance = piggyAccount.getBalance();
                    BigDecimal rate = goal.getInterestRate(); // per-goal interest rate
                    BigDecimal dailyRate = rate.divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, RoundingMode.HALF_UP);
                    BigDecimal dailyInterest = balance.multiply(dailyRate).setScale(4, RoundingMode.HALF_UP);

                    if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {
                        // Add to accrued interest, not balance
                        goal.setAccruedInterest(goal.getAccruedInterest().add(dailyInterest));
                        goal.setLastInterestCalculatedAt(LocalDateTime.now());

                        piggyGoalRepository.save(goal); // save updated accrued interest

                        // Optional: record transaction ledger
                        transactionService.createInterestTransaction(piggyAccount, dailyInterest);

                        log.debug("Added daily interest {} to goal {} (balance: {}, rate: {})",
                                dailyInterest, goal.getId(), balance, rate);
                    }
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Error processing interest for user: " + user.getId(), e);
            }
            return null;
        });
    }

    private void handleMaturity(PiggyGoalModel goal) {
        AccountModel piggyAccount = goal.getAccountModel();
        if (piggyAccount == null) {
            log.warn("Matured goal {} has no account. Skipping.", goal.getId());
            return;
        }

        BigDecimal interest = goal.getAccruedInterest();
        BigDecimal balance = piggyAccount.getBalance();

        // Add accrued interest to balance
        piggyAccount.setBalance(balance.add(interest));
        piggyAccount = accountRepository.save(piggyAccount);

        // Reset accrued interest and mark goal as completed
        goal.setAccruedInterest(BigDecimal.ZERO);
        goal.setCompletedAt(LocalDateTime.now());
        goal.setStatus(GoalStatus.COMPLETED);
        piggyGoalRepository.save(goal);

        log.info("Goal {} matured. Interest {} applied. Balance now {}", goal.getId(), interest, piggyAccount.getBalance());
    }
}