package com.example.piggy_saving.services.scheduler;

import com.example.piggy_saving.models.*;
import com.example.piggy_saving.models.enums.GoalStatus;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.repository.InterestPaymentRepository;
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
    private final InterestPaymentRepository interestPaymentRepository;
    private final TransactionService transactionService;
    private final PlatformTransactionManager transactionManager;

    private static final int DAYS_IN_YEAR = 365;

    /**
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateDailyInterest() {
        log.info("Starting daily interest calculation...");

        // Fetch all active goals
        List<PiggyGoalModel> activeGoals = piggyGoalRepository.findByStatus(GoalStatus.ACTIVE);
        if (activeGoals.isEmpty()) {
            log.info("No active piggy goals found. Exiting.");
            return;
        }

        // Group by user for transaction safety
        Map<UserModel, List<PiggyGoalModel>> goalsByUser =
                activeGoals.stream().collect(Collectors.groupingBy(PiggyGoalModel::getUserModel));

        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<UserModel, List<PiggyGoalModel>> entry : goalsByUser.entrySet()) {
            UserModel user = entry.getKey();
            List<PiggyGoalModel> userGoals = entry.getValue();

            try {
                processUserGoals(user, userGoals);
                successCount++;
                log.info("Processed interest for user {}", user.getId());
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to process user {}: {}", user.getId(), e.getMessage(), e);
            }
        }

        log.info("Daily interest calculation completed. Success: {}, Failed: {}", successCount, failureCount);
    }

    private void processUserGoals(UserModel user, List<PiggyGoalModel> goals) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute((TransactionCallback<Void>) status -> {
            for (PiggyGoalModel goal : goals) {
                AccountModel piggyAccount = goal.getAccountModel();
                if (piggyAccount == null) {
                    log.warn("Goal {} has no associated account. Skipping.", goal.getId());
                    continue;
                }

                // Check maturity
                if (goal.getLockExpiresAt() != null && LocalDateTime.now().isAfter(goal.getLockExpiresAt())) {
                    handleGoalMaturity(goal, piggyAccount);
                    continue;
                }

                // Prevent double calculation for today
                if (goal.getLastInterestCalculatedAt() != null &&
                        goal.getLastInterestCalculatedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                    continue;
                }

                // Calculate daily interest
                BigDecimal balance = piggyAccount.getBalance();
                BigDecimal dailyRate = goal.getInterestRate()
                        .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 10, RoundingMode.HALF_UP);
                BigDecimal dailyInterest = balance.multiply(dailyRate).setScale(4, RoundingMode.HALF_UP);

                if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {
                    // Create interest payment record
                    TransactionModel txn = transactionService.createInterestTransaction(piggyAccount, dailyInterest);

                    InterestPaymentModel payment = InterestPaymentModel.builder()
                            .piggyGoalModel(goal)
                            .amount(dailyInterest)
                            .transactionModel(txn)
                            .paymentDate(LocalDateTime.now().toLocalDate())
                            .build();
                    interestPaymentRepository.save(payment);

                    // Update last calculated timestamp
                    goal.setLastInterestCalculatedAt(LocalDateTime.now());
                    piggyGoalRepository.save(goal);

                    log.debug("Added daily interest {} to goal {} (balance: {}, rate: {})",
                            dailyInterest, goal.getId(), balance, goal.getInterestRate());
                }
            }
            return null;
        });
    }

    private void handleGoalMaturity(PiggyGoalModel goal, AccountModel piggyAccount) {
        // Sum all interest payments for the goal
        BigDecimal totalInterest = interestPaymentRepository
                .findByPiggyGoalModel(goal)
                .stream()
                .map(InterestPaymentModel::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add principal + interest to account
        BigDecimal newBalance = piggyAccount.getBalance().add(totalInterest);
        piggyAccount.setBalance(newBalance);
        accountRepository.save(piggyAccount);

        // Update goal status
        goal.setStatus(GoalStatus.COMPLETED);
        goal.setCompletedAt(LocalDateTime.now());
        piggyGoalRepository.save(goal);

        log.info("Goal {} matured. Total interest {} applied. New balance {}", goal.getId(), totalInterest, newBalance);
    }
}