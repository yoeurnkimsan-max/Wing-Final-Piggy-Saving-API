package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.models.NotificationModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.repository.NotificationRepository;
import com.example.piggy_saving.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
