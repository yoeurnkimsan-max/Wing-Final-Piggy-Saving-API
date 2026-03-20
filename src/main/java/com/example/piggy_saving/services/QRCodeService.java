package com.example.piggy_saving.services;

import com.google.zxing.WriterException;
import java.io.IOException;
import java.util.UUID;

public interface QRCodeService {

    // Generate QR code from any text
    byte[] generateQRCode(String text, int width, int height) throws WriterException, IOException;

    // Generate payload string for static QR (MAIN account)
    String generateStaticQr(UUID userId);

    // Generate PNG image bytes for static QR (MAIN account)
    byte[] generateStaticQrImage(UUID userId, int width, int height) throws WriterException, IOException;
}