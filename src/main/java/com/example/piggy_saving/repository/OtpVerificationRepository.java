package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.OtpVerificationModel;
import com.example.piggy_saving.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerificationModel, UUID> {
    OtpVerificationModel findTopByUserModelOrderByCreatedAtDesc(UserModel user);
}
