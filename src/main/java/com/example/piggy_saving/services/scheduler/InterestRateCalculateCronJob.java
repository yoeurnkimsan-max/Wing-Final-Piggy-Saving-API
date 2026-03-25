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
    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(5000); // USD 5,000

    /**
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateAndApplyInterest() {
        log.info("Starting daily interest calculation");

        // Fetch all active piggy goals
        List<PiggyGoalModel> activeGoals = piggyGoalRepository.findByStatus(GoalStatus.ACTIVE);
        if (activeGoals.isEmpty()) {
            log.info("No active piggy goals found. Exiting.");
            return;
        }

        // Group by user to process each user in a separate transaction
        Map<UserModel, List<PiggyGoalModel>> goalsByUser = activeGoals.stream()
                .collect(Collectors.groupingBy(PiggyGoalModel::getUserModel));

        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<UserModel, List<PiggyGoalModel>> entry : goalsByUser.entrySet()) {
            UserModel user = entry.getKey();
            List<PiggyGoalModel> userGoals = entry.getValue();

            // Process each user in its own transaction
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

    /**
     * Process interest for a single user. This method runs in its own transaction.
     */
    private void processUserInterest(UserModel user, List<PiggyGoalModel> userGoals) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute((TransactionCallback<Void>) status -> {
            try {
                // Get user's main account balance (within the transaction)
                AccountModel mainAccount = accountRepository
                        .findAccountModelsByUserModelIdAndAccountType(user.getId(), AccountType.MAIN)
                        .orElse(null);
                if (mainAccount == null) {
                    log.warn("User {} has no main account, skipping interest calculation", user.getId());
                    return null;
                }

                BigDecimal mainBalance = mainAccount.getBalance();
                double rate = getInterestRate(mainBalance);
                BigDecimal dailyRate = BigDecimal.valueOf(rate / DAYS_IN_YEAR);

                for (PiggyGoalModel goal : userGoals) {
                    AccountModel piggyAccount = goal.getAccountModel();
                    if (piggyAccount == null) {
                        log.warn("Piggy goal {} has no associated account, skipping", goal.getId());
                        continue;
                    }

                    LocalDateTime lockExpiresAt = goal.getLockExpiresAt();
                    if (lockExpiresAt != null && lockExpiresAt.isBefore(LocalDateTime.now())) {
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
            } catch (Exception e) {
                // Rollback the transaction for this user
                status.setRollbackOnly();
                throw new RuntimeException("Error processing interest for user: " + user.getId(), e);
            }
            return null;
        });
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