package com.example.piggy_saving.controllers;


import com.example.piggy_saving.dto.request.QRPaymentPayload;
import com.example.piggy_saving.dto.request.QRTransferPayloadRequestDto;
import com.example.piggy_saving.dto.request.TransferRequestDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.TransferContributeResponseDto;
import com.example.piggy_saving.dto.response.TransferP2PResponseDto;
import com.example.piggy_saving.dto.response.TransferResponseDto;
import com.example.piggy_saving.mappers.QRTransferMapper;
import com.example.piggy_saving.models.enums.TransferType;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.TransferService;
import com.example.piggy_saving.util.DecodeBase64ToObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(QRTransferController.BASE_ROUTE)
@RequiredArgsConstructor
public class QRTransferController {
    public static final String BASE_ROUTE = "/api/v1/qr-transfers";

    private final DecodeBase64ToObject decodeBase64ToObject;
    private final TransferService transferService;
    private final QRTransferMapper qrTransferMapper;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<?>> generateQRPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody QRTransferPayloadRequestDto payload) {


        QRPaymentPayload requestPayload =
                decodeBase64ToObject.decodeBase64ToObject(payload.getQrBase64String(), QRPaymentPayload.class);

        TransferRequestDto transfer = TransferRequestDto.builder()
                .recipientAccountNumber(requestPayload.getRecipientAccountNumber())
                .amount(payload.getAmount())
                .notes(payload.getNotes())
                .build();


        switch (requestPayload.getType()) {
            case TransferType.P2P: {

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
            case TransferType.CONTRIBUTION:

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
            default:
                return ResponseEntity.badRequest().build();
        }
    }
}
