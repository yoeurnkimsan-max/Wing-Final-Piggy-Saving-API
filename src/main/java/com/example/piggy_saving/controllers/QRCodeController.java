package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.QRContributeParserRequestDto;
import com.example.piggy_saving.dto.response.QRContributeDataResponse;
import com.example.piggy_saving.exception.UnauthorizedException;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.QRCodeParserService;
import com.example.piggy_saving.services.QRCodeService;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(QRCodeController.BASE_ROUTE)
@RequiredArgsConstructor  // ← Changed from @AllArgsConstructor
public class QRCodeController {
    public static final String BASE_ROUTE = "/api/v1/qr";
    private final QRCodeService qrCodeService;
    private final QRCodeParserService qrCodeParserService;

    @Value("${app.base-url:http://localhost:8080}")  // Added default for safety
    private String baseUrl;

    @GetMapping(value = "/p2p", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateP2PQRCode(@RequestParam String userAccountNumber) {
        String url = baseUrl + "/send?to=" + userAccountNumber;
        try {
            return qrCodeService.generateQRCode(url, 300, 300);
        } catch (Exception e) {
            throw new RuntimeException("QR code generation failed", e);
        }
    }

    @GetMapping("/qrcode/static")
    public ResponseEntity<byte[]> getStaticQr(@AuthenticationPrincipal CustomUserDetails userDetails) throws WriterException, IOException {
        byte[] qrImage = qrCodeService.generateStaticQrImage(userDetails.getUserId(), 300, 300);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(qrImage);
    }

    @GetMapping("/piggy/{piggyGoalNumber}")
    public ResponseEntity<byte[]> generateContributeQRCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String piggyGoalNumber
    ) throws WriterException, IOException {

        // 1. Check if user is authenticated
        if (userDetails == null) {
            throw new UnauthorizedException("You must be logged in to generate QR code");
        }

        // 2. Generate QR image
        byte[] qrImage = qrCodeService.generatePiggyQrImage(
                piggyGoalNumber,
                userDetails.getUserId(),
                300,
                300
        );

        // 3. Return the image as PNG
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(qrImage);
    }


    @PostMapping("/parse-qr")
    public ResponseEntity<QRContributeDataResponse> parseQr(@RequestBody QRContributeParserRequestDto requestDto) {
        return ResponseEntity.ok(qrCodeParserService.parseQrPayload(requestDto));
    }
}