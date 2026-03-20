package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.GenerateQrRequestDto;
import com.example.piggy_saving.dto.response.GenerateQrResponseDto;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.services.QrPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrPaymentServiceImpl implements QrPaymentService {
    private final AccountRepository accountRepository;
//    private final QR

    @Override
    public GenerateQrResponseDto generateQr(UUID userId, GenerateQrRequestDto request) {
        return null;
    }
}
