package com.example.piggy_saving.exception;

public class QRException extends RuntimeException {

    private final String errorCode;

    public QRException(String message) {
        super(message);
        this.errorCode = "QR_ERROR";
    }

    public QRException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public QRException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "QR_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
