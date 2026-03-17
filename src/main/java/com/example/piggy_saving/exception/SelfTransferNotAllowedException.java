package com.example.piggy_saving.exception;

public class SelfTransferNotAllowedException extends RuntimeException {
    public SelfTransferNotAllowedException(String message) {
        super(message);
    }
}
