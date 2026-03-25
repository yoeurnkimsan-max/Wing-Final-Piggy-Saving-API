package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.NotificationDto;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationDto> notifications = notificationService.getUserNotifications(userDetails.getUserId(), page, size);
        ApiResponse<Page<NotificationDto>> response = ApiResponse.<Page<NotificationDto>>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Notifications retrieved")
                .timestamp(LocalDateTime.now())
                .data(notifications)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        long count = notificationService.getUnreadCount(userDetails.getUserId());
        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Unread count retrieved")
                .timestamp(LocalDateTime.now())
                .data(count)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID notificationId) {

        notificationService.markAsRead(userDetails.getUserId(), notificationId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Notification marked as read")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAllAsRead(userDetails.getUserId());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("All notifications marked as read")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
