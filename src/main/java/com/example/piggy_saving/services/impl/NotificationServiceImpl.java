package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.response.NotificationDto;
import com.example.piggy_saving.models.NotificationModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.repository.NotificationRepository;
import com.example.piggy_saving.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    @Override
    public void notify(UserModel user, String message) {
        NotificationModel notificationModel = NotificationModel.builder()
                .userModel(user)
                .message(message)
                .build();
        notificationRepository.save(notificationModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(UUID userId, int page, int size) {
        Page<NotificationModel> notifications = notificationRepository.findByUserModelIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return notifications.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserModelIdAndIsReadFalse(userId, false);
    }

    @Override
    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        notificationRepository.markAsRead(userId, notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationDto toDto(NotificationModel notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
