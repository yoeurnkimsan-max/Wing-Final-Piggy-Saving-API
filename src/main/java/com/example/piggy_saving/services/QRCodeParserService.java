package com.example.piggy_saving.services;

import com.example.piggy_saving.dto.request.GenerateQrRequestDto;
import com.example.piggy_saving.dto.request.QRContributeParserRequestDto;
import com.example.piggy_saving.dto.response.GenerateQrResponseDto;
import com.example.piggy_saving.dto.response.QRContributeDataResponse;

import java.util.UUID;

public interface QRCodeParserService {
    QRContributeDataResponse parseQrPayload(QRContributeParserRequestDto requestDto);
}
