package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.QRPaymentPayload;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.models.enums.TransferType;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
public class QRGenerationController {

    private final QRCodeService qrCodeService;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;

    private static final int QR_EXPIRY_MINUTES = 10;

    /**
     * Generate QR for transferring Main → Piggy (Own Transfer)
     * GET /api/v1/qr/generate/own-transfer?accountPiggyNumber=176051863334
     */
    @GetMapping(value = "/generate/own-transfer", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateOwnTransferQR(
            @RequestParam String accountPiggyNumber) throws Exception {

        QRPaymentPayload payload = QRPaymentPayload.builder()
                .type(TransferType.OWN)
                .accountPiggyNumber(accountPiggyNumber)
                .expiresAt(LocalDateTime.now().plusMinutes(QR_EXPIRY_MINUTES)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .version("1.0")
                .build();

        String encodedPayload = encodePayload(payload);
        return qrCodeService.generateQRCode(encodedPayload, 300, 300);
    }


    /**
     * Generate QR for Contribution to Piggy Goal
     * GET /api/v1/qr/generate/contribute?piggyAccountNumber=177152031515
     */
    @GetMapping(value = "/generate/contribute", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateContributeQR(
            @RequestParam String piggyAccountNumber) throws Exception {

        QRPaymentPayload payload = QRPaymentPayload.builder()
                .type(TransferType.CONTRIBUTION)
                .piggyAccountNumber(piggyAccountNumber)
                .expiresAt(LocalDateTime.now().plusMinutes(QR_EXPIRY_MINUTES)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .version("1.0")
                .build();

        String encodedPayload = encodePayload(payload);
        return qrCodeService.generateQRCode(encodedPayload, 300, 300);
    }

    /**
     *
     * @param userDetails
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/generate/p2p-transfer-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateMainQR(
            @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {

        AccountModel accountModel = accountRepository.findAccountModelsByUserModelIdAndAccountType(userDetails.getUserId(), AccountType.MAIN)
                .orElseThrow(() -> new Exception("Account not found"));

        QRPaymentPayload payload = QRPaymentPayload.builder()
                .type(TransferType.P2P)
                .recipientAccountNumber(accountModel.getAccountNumber())
                .expiresAt(LocalDateTime.now().plusMinutes(QR_EXPIRY_MINUTES)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .version("1.0")
                .build();

        String encodedPayload = encodePayload(payload);
        return qrCodeService.generateQRCode(encodedPayload, 300, 300);
    }

    private <T> String encodePayload(T payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }
}