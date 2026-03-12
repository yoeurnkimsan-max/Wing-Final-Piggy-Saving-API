package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.RegisterRequestDto;
import com.example.piggy_saving.dto.response.RegisterResponseDto;
import com.example.piggy_saving.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Override
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        return null;
    }
}
