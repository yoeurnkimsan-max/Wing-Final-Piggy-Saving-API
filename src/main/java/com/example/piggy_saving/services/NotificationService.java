package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.response.NotificationDto;
import com.example.piggy_saving.event.P2PTransferCompletedEvent;
import com.example.piggy_saving.models.UserModel;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {
    void notify(UserModel user, String message);

    /**
     * Get paginated notifications for a user.
     */
    Page<NotificationDto> getUserNotifications(UUID userId, int page, int size);

    /**
     * Get count of unread notifications.
     */
    long getUnreadCount(UUID userId);

    /**
     * Mark a single notification as read.
     */
    void markAsRead(UUID userId, UUID notificationId);

    /**
     * Mark all notifications as read.
     */
    void markAllAsRead(UUID userId);

}
