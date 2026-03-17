package com.example.piggy_saving.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private int statusCode;
    private String statusMessage;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
