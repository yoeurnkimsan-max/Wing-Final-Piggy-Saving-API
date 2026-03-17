package com.example.piggy_saving.exception.handler;

import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.error.ErrorResponse;
import com.example.piggy_saving.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle resource not found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    //    Handle account not found
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountNotFound(
            AccountNotFoundException ex, HttpServletRequest request) {

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    // Handle user already exists (409 Conflict)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        ErrorResponse error = buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    /**
     * Role Not Found
     */

    @ExceptionHandler(RoleNotFoundExceptionHandler.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(
            RoleNotFoundExceptionHandler exceptionHandler, HttpServletRequest request
    ){
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exceptionHandler.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * OTP send failed
     */

    @ExceptionHandler(OtpFailedToSentExceptionHandler.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(
            OtpFailedToSentExceptionHandler exceptionHandler, HttpServletRequest request
    ){
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exceptionHandler.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Handle validation errors from @Valid (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handle authentication errors (these usually come from Spring Security filters,
    // but if they bubble up to the controller, they can be caught here)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest request) {
        ErrorResponse error = buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "User account is disabled",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Handle any other unhandled exceptions (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        // Log the exception stack trace here for debugging
        ex.printStackTrace(); // replace with proper logger

        ErrorResponse error = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Required request body is missing or malformed",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    // Helper method to build simple error responses
    private ErrorResponse buildErrorResponse(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}