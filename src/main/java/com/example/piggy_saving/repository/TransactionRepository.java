package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionModel, UUID> {

    @Query("SELECT t FROM TransactionModel t WHERE t.initiatedByUserModel.id = :userId")
    List<TransactionModel> findAllByInitiatedByUserModelId(@Param("userId") UUID userId);
}
