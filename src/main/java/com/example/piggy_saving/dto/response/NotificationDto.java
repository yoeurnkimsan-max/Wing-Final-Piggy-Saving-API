package com.example.piggy_saving.dto.response;

import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotificationDto {

    private UUID id;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}