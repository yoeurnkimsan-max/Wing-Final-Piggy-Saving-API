package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.InterestPaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterestPaymentRepository extends JpaRepository<InterestPaymentModel, UUID> {
}
