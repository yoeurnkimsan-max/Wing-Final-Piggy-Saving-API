package com.example.piggy_saving.services.scheduler;

import com.example.piggy_saving.models.PiggyGoalModel;
import com.example.piggy_saving.models.enums.GoalStatus;
import com.example.piggy_saving.repository.PiggyGoalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoalExpiryCronJob {

    private final PiggyGoalRepository piggyGoalRepository;
    private static final Logger log = LoggerFactory.getLogger(GoalExpiryCronJob.class);

    @Scheduled(cron = "0 0 * * * *") // runs at the top of every hour
    public void checkGoalExpiry() {
        LocalDateTime now = LocalDateTime.now();
        List<PiggyGoalModel> expiredGoals = piggyGoalRepository.findByEndAtBeforeAndStatusNot(now, GoalStatus.BROKEN);

        if (!expiredGoals.isEmpty()) {
            expiredGoals.forEach(goal -> goal.setStatus(GoalStatus.BROKEN));
            piggyGoalRepository.saveAll(expiredGoals);
            expiredGoals.forEach(goal -> log.info("Goal expired: {}", goal.getId()));
        }
    }
}