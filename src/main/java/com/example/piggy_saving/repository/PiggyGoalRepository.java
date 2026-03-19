package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.PiggyGoalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PiggyGoalRepository extends JpaRepository<PiggyGoalModel, UUID> {


    Optional<PiggyGoalModel> findByIdAndUserModelId(UUID id, UUID userModelId);

    Optional<PiggyGoalModel> findById(UUID id);

    @Query("SELECT p FROM PiggyGoalModel p WHERE p.id = :id AND p.status = 'ACTIVE'")
    Optional<PiggyGoalModel> findPiggyGoalActiveById(@Param("id") UUID id);
}
