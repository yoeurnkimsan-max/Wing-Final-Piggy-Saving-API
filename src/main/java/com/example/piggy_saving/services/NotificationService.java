package com.example.piggy_saving.services;

import com.example.piggy_saving.event.P2PTransferCompletedEvent;
import com.example.piggy_saving.models.UserModel;

public interface NotificationService {
    void notify(UserModel user, String message);
}
