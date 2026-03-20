package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.QRContributeParserRequestDto;
import com.example.piggy_saving.dto.response.QRContributeDataResponse;
import com.example.piggy_saving.models.enums.TransferType;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.services.QRCodeParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QRCodeParserServiceImpl implements QRCodeParserService {
    private final AccountRepository accountRepository;

    /**
     * Convert a QR payload like "PAY|ACC:12345|NAME:John|GOAL:Vacation" into JSON object
     */
    @Override
    public QRContributeDataResponse parseQrPayload(QRContributeParserRequestDto requestDto) {
        if (requestDto == null || requestDto.getQrText() == null || !requestDto.getQrText().startsWith("PAY|")) {
            throw new IllegalArgumentException("Invalid QR payload format");
        }

        String qrText = requestDto.getQrText(); // Extract string from DTO
        String[] parts = qrText.split("\\|");

        String accountNumber = null;
        String fullName = null;
        String goalName = null;

        for (String part : parts) {
            if (part.startsWith("ACC:")) {
                accountNumber = part.substring(4);
            } else if (part.startsWith("NAME:")) {
                fullName = part.substring(5);
            } else if (part.startsWith("GOAL:")) {
                goalName = part.substring(5);
            }
        }

        if (accountNumber == null || fullName == null) {
            throw new IllegalArgumentException("QR payload missing required fields");
        }

        return QRContributeDataResponse.builder()
                .accountNumber(accountNumber)
                .fullName(fullName)
                .goalName(goalName)
                .type(TransferType.CONTRIBUTION)
                .build();
    }
//    private final QR


}
