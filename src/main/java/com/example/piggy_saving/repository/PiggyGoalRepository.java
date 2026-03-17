package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.PiggyGoalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PiggyGoalRepository extends JpaRepository<PiggyGoalModel, UUID> {
    @Query("SELECT pg FROM PiggyGoalModel pg " +
            "LEFT JOIN FETCH pg.accountModel a " +
            "WHERE pg.id = :goalId")
    PiggyGoalModel findByIdWithAccount(@Param("goalId") UUID goalId);
}
