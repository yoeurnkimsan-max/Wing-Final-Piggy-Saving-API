package com.example.piggy_saving.exception;

public class OtpFailedToSentExceptionHandler extends RuntimeException {
    public OtpFailedToSentExceptionHandler(String message) {
        super(message);
    }
}
