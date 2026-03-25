package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.NotificationModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationModel, UUID> {
    Page<NotificationModel> findByUserModelIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    //    findByUserModelIdOrderByCreatedAtDesc
    long countByUserModelIdAndIsReadFalse(UUID userModelId, boolean isRead);
    @Modifying
    @Transactional
    @Query("UPDATE NotificationModel n SET n.isRead = true WHERE n.userModel.id = :userId AND n.id = :notificationId")
    void markAsRead(@Param("userId") UUID userId, @Param("notificationId") UUID notificationId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationModel n SET n.isRead = true WHERE n.userModel.id = :userId")
    void markAllAsRead(@Param("userId") UUID userId);
}