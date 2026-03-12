package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.RegisterRequestDto;
import com.example.piggy_saving.dto.response.RegisterResponseDto;

public interface AuthService {

    RegisterResponseDto register(RegisterRequestDto registerRequestDto);
}
