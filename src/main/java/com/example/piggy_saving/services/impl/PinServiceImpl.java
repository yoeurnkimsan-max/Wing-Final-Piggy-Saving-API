package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.services.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PinServiceImpl implements PinService {

    private final PasswordEncoder passwordEncoder;
    @Override
    public String hashPin(String rawPin) {
        return passwordEncoder.encode(rawPin);
    }

    @Override
    public boolean verifyPin(String rawPin, String storedHash) {
        return passwordEncoder.matches(rawPin, storedHash);
    }
}
