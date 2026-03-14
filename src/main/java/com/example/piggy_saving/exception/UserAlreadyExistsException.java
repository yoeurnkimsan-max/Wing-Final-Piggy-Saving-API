package com.example.piggy_saving.exception;

import org.springframework.http.HttpStatusCode;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(HttpStatusCode statusCode, String message){
        super(message);
    }
}
