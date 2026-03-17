package com.example.piggy_saving.exception;

public class NotFoundExceptionHandler extends RuntimeException {
    public NotFoundExceptionHandler(String message) {
        super(message);
    }
}
