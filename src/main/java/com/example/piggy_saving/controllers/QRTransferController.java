package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.QRPaymentPayload;
import com.example.piggy_saving.dto.request.QRTransferPayloadRequestDto;
import com.example.piggy_saving.dto.request.TransferRequestDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.TransferContributeResponseDto;
import com.example.piggy_saving.dto.response.TransferP2PResponseDto;
import com.example.piggy_saving.mappers.QRTransferMapper;
import com.example.piggy_saving.models.enums.TransferType;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.TransferService;
import com.example.piggy_saving.util.DecodeBase64ToObject;
import com.example.piggy_saving.util.HmacUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(QRTransferController.BASE_ROUTE)
@RequiredArgsConstructor
public class QRTransferController {
    public static final String BASE_ROUTE = "/api/v1/qr-transfers";

    private final DecodeBase64ToObject decodeBase64ToObject;
    private final TransferService transferService;
    private final QRTransferMapper qrTransferMapper;
    private final HmacUtils hmacUtils; // injected for HMAC verification

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<?>> generateQRPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody QRTransferPayloadRequestDto payload) {

        // 1. Decode base64 to QRPaymentPayload object
        QRPaymentPayload requestPayload;
        try {
            requestPayload = decodeBase64ToObject.decodeBase64ToObject(payload.getQrBase64String(), QRPaymentPayload.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid QR code format")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        // 2. Validate required fields exist
        if (requestPayload.getType() == null ||
                requestPayload.getRecipientAccountNumber() == null ||
                requestPayload.getExpiresAt() == null ||
                requestPayload.getVersion() == null ||
                requestPayload.getSignature() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message("Missing required fields in QR payload")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        // 3. Check expiration
        try {
            LocalDateTime expiresAt = LocalDateTime.parse(requestPayload.getExpiresAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (expiresAt.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .success(false)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message("QR code has expired")
                                .timestamp(LocalDateTime.now())
                                .build()
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid expiration date format")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        // 4. Recompute HMAC and compare
        // Build map of fields in the same order as generation (without signature)
        Map<String, Object> fieldsForHmac = new LinkedHashMap<>();
        fieldsForHmac.put("type", requestPayload.getType().toString());
        fieldsForHmac.put("recipient_account_number", requestPayload.getRecipientAccountNumber());
        fieldsForHmac.put("expires_at", requestPayload.getExpiresAt());
        fieldsForHmac.put("version", requestPayload.getVersion());

        String jsonForHmac;
        try {
            jsonForHmac = new ObjectMapper().writeValueAsString(fieldsForHmac);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal error while processing QR")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        String computedSignature;
        try {
            computedSignature = hmacUtils.computeHmac(jsonForHmac);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Signature verification failed")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        if (!computedSignature.equals(requestPayload.getSignature())) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid QR code signature – tampering detected")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        // 5. Build transfer request
        TransferRequestDto transfer = TransferRequestDto.builder()
                .recipientAccountNumber(requestPayload.getRecipientAccountNumber())
                .amount(payload.getAmount())
                .notes(payload.getNotes())
                .build();

        // 6. Process based on type
        switch (requestPayload.getType()) {
            case P2P: {
                TransferP2PResponseDto responseService = transferService.transferP2P(userDetails.getUserId(), transfer);
                ApiResponse<TransferP2PResponseDto> apiResponse = ApiResponse.<TransferP2PResponseDto>builder()
                        .success(true)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("OK")
                        .timestamp(LocalDateTime.now())
                        .data(responseService)
                        .message("Transfer completed successfully")
                        .build();
                return ResponseEntity.ok(apiResponse);
            }
            case CONTRIBUTION: {
                TransferContributeResponseDto responseService = transferService.transferContribute(userDetails.getUserId(), transfer);
                ApiResponse<TransferContributeResponseDto> apiResponse = ApiResponse.<TransferContributeResponseDto>builder()
                        .success(true)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("OK")
                        .timestamp(LocalDateTime.now())
                        .data(responseService)
                        .message("Transfer completed successfully")
                        .build();
                return ResponseEntity.ok(apiResponse);
            }
            default:
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .success(false)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message("Unsupported transfer type")
                                .timestamp(LocalDateTime.now())
                                .build()
                );
        }
    }
}