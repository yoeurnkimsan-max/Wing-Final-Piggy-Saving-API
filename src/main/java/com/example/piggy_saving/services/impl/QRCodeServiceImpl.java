package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.services.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRCodeServiceImpl implements QRCodeService {

    private final AccountRepository accountRepository;

    @Override
    public byte[] generateQRCode(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }

    @Override
    public String generateStaticQr(UUID userId) {
        AccountModel account = accountRepository
                .findByUserModelIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new RuntimeException("Main account not found"));

        return buildQrPayload(account.getAccountNumber(), account.getUserModel().getName());
    }

    @Override
    public byte[] generateStaticQrImage(UUID userId, int width, int height) throws WriterException, IOException {
        String qrPayload = generateStaticQr(userId);
        return generateQRCode(qrPayload, width, height);
    }

    private String buildQrPayload(String accountNumber, String fullName) {
        return String.format("PAY|ACC:%s|NAME:%s", accountNumber, fullName);
    }
}