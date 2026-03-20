package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.GenerateQrRequestDto;
import com.example.piggy_saving.dto.response.GenerateQrResponseDto;

import java.util.UUID;

public interface QrPaymentService {
    GenerateQrResponseDto generateQr(UUID userId, GenerateQrRequestDto request);

}
