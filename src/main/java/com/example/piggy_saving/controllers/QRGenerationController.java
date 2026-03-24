package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.QRPaymentPayload;
import com.example.piggy_saving.dto.request.QRValidationResponse;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.exception.BadRequestException;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.enums.AccountType;
import com.example.piggy_saving.models.enums.TransferType;
import com.example.piggy_saving.repository.AccountRepository;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.QRCodeService;
import com.example.piggy_saving.util.DecodeBase64ToObject;
import com.example.piggy_saving.util.HmacUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
public class QRGenerationController {

    private final QRCodeService qrCodeService;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;
    private final HmacUtils hmacUtils;
    private final DecodeBase64ToObject decodeBase64ToObject;

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
                .recipientAccountNumber(accountPiggyNumber)
                .expiresAt(LocalDateTime.now().plusMinutes(QR_EXPIRY_MINUTES)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .version("1.0")
                .build();

        String encodedPayload = encodePayloadWithSignature(payload);
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
                .recipientAccountNumber(piggyAccountNumber)
                .expiresAt(LocalDateTime.now().plusMinutes(QR_EXPIRY_MINUTES)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .version("1.0")
                .build();

        String encodedPayload = encodePayloadWithSignature(payload);
        return qrCodeService.generateQRCode(encodedPayload, 300, 300);
    }

    /**
     * Generate QR for P2P transfer (main account)
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

        String encodedPayload = encodePayloadWithSignature(payload);
        return qrCodeService.generateQRCode(encodedPayload, 300, 300);
    }

    // QRGenerationController.java (or a dedicated QRValidationController)

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<QRValidationResponse>> validateQR(@RequestParam String qrBase64) {
        try {
            // 1. Decode and parse
            QRPaymentPayload payload = decodeBase64ToObject.decodeBase64ToObject(qrBase64, QRPaymentPayload.class);

            // 2. Validate required fields
            if (payload.getType() == null || payload.getRecipientAccountNumber() == null ||
                    payload.getExpiresAt() == null || payload.getVersion() == null || payload.getSignature() == null) {
                throw new BadRequestException("Invalid QR Code");
            }

            // 3. Check expiration
            LocalDateTime expiresAt = LocalDateTime.parse(payload.getExpiresAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (expiresAt.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("QR code has expired");
            }

            // 4. Recompute HMAC and compare
            Map<String, Object> fieldsForHmac = new LinkedHashMap<>();
            fieldsForHmac.put("type", payload.getType().toString());
            fieldsForHmac.put("recipient_account_number", payload.getRecipientAccountNumber());
            fieldsForHmac.put("expires_at", payload.getExpiresAt());
            fieldsForHmac.put("version", payload.getVersion());
            String jsonForHmac = objectMapper.writeValueAsString(fieldsForHmac);
            String computedSignature = hmacUtils.computeHmac(jsonForHmac);

            if (!computedSignature.equals(payload.getSignature())) {
                throw new BadRequestException("Invalid QR signature – tampering detected");
            }

            // 5. Build response with recipient info
            QRValidationResponse response = QRValidationResponse.builder()
                    .type(payload.getType())
                    .recipientAccountNumber(payload.getRecipientAccountNumber())
                    .expiresAt(payload.getExpiresAt())
                    .build();

            ApiResponse<QRValidationResponse> apiResponse = ApiResponse.<QRValidationResponse>builder()
                    .success(true)
                    .statusCode(HttpStatus.OK.value())
                    .message("QR is valid")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(

                    ApiResponse.<QRValidationResponse>builder()
                            .success(false)
                            .message("Invalid QR data")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
    }

    /**
     * Encode the payload with HMAC signature
     */
    private String encodePayloadWithSignature(QRPaymentPayload payload) throws Exception {
        // 1. Build a map of fields in a fixed order (for HMAC calculation)
        Map<String, Object> fieldsForHmac = new LinkedHashMap<>();
        fieldsForHmac.put("type", payload.getType());
        fieldsForHmac.put("recipient_account_number", payload.getRecipientAccountNumber());
        fieldsForHmac.put("expires_at", payload.getExpiresAt());
        fieldsForHmac.put("version", payload.getVersion());

        // 2. Serialize to JSON and compute HMAC signature
        String jsonForHmac = objectMapper.writeValueAsString(fieldsForHmac);
        String signature = hmacUtils.computeHmac(jsonForHmac);
        payload.setSignature(signature);   // set signature on the payload

        // 3. Serialize full payload (including signature) and base64 encode
        String fullJson = objectMapper.writeValueAsString(payload);
        return Base64.getEncoder().encodeToString(fullJson.getBytes());
    }
}