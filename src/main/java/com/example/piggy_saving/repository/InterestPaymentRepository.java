package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.InterestPaymentModel;
import com.example.piggy_saving.models.PiggyGoalModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterestPaymentRepository extends JpaRepository<InterestPaymentModel, UUID> {
    List<InterestPaymentModel> findByPiggyGoalModel(PiggyGoalModel piggyGoalModel);
}
