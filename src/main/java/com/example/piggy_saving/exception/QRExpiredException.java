package com.example.piggy_saving.exception;

public class QRExpiredException extends RuntimeException {
    public QRExpiredException(String message) {
        super(message);
    }
}
