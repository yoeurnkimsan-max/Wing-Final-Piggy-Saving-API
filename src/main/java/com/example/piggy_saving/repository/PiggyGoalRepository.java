package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.PiggyGoalModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PiggyGoalRepository extends JpaRepository<PiggyGoalModel, UUID> {
}
