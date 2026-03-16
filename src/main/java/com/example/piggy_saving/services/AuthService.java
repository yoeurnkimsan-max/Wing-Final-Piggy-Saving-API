package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.LoginRequestDto;
import com.example.piggy_saving.dto.request.RegisterRequestDto;
import com.example.piggy_saving.dto.response.LoginResponseDto;
import com.example.piggy_saving.dto.response.RegisterResponseDto;

public interface AuthService {

    RegisterResponseDto register(RegisterRequestDto registerRequestDto);

    LoginResponseDto login(LoginRequestDto loginRequestDto);

    LoginResponseDto verifyOtp(String email, String otpCode);
}
