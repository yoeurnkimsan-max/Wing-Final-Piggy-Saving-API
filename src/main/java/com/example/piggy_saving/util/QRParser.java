package com.example.piggy_saving.util;

import com.example.piggy_saving.dto.response.QRContributeDataResponse;
import com.example.piggy_saving.models.enums.TransferType;

public class QRParser {

    /**
     * Convert QR string into JSON-friendly object
     * Example QR formats:
     * P2P: PAY|ACC:<accountNumber>|NAME:<fullName>
     * Piggy Goal: PAY|ACC:<accountNumber>|NAME:<fullName>|GOAL:<goalName>
     */
    public static QRContributeDataResponse parse(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            throw new IllegalArgumentException("QR data is empty");
        }

        String[] parts = qrData.split("\\|");
        String accountNumber = null;
        String fullName = null;
        String goalName = null;

        for (String part : parts) {
            if (part.startsWith("ACC:")) accountNumber = part.substring(4);
            else if (part.startsWith("NAME:")) fullName = part.substring(5);
            else if (part.startsWith("GOAL:")) goalName = part.substring(5);
        }

        return QRContributeDataResponse.builder()
                .accountNumber(accountNumber)
                .fullName(fullName)
                .goalName(goalName)
                .type(goalName != null ? TransferType.CONTRIBUTION : TransferType.P2P)
                .build();
    }
}