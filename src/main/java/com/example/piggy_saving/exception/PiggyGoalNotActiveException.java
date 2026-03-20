package com.example.piggy_saving.exception;

public class PiggyGoalNotActiveException extends RuntimeException {
    public PiggyGoalNotActiveException(){
        super("Piggy goal is not active");
    }
    public PiggyGoalNotActiveException(String message) {
        super(message);
    }
}
