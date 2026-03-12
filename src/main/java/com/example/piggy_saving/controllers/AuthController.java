package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.RegisterRequestDto;
import com.example.piggy_saving.dto.response.RegisterResponseDto;
import com.example.piggy_saving.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthController.BASE_ROUTE)
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    public static final String BASE_ROUTE = "/auth/v1"; // made public for reuse

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // You can add other auth endpoints like login, refresh, etc.
}