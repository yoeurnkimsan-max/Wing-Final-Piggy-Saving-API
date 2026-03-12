package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.NotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationModel, UUID> {
}
