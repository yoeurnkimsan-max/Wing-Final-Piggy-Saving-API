package com.example.piggy_saving.services;


public interface PinService {
    String hashPin(String rawPin);
    boolean verifyPin(String rawPin, String storedHash);
}
