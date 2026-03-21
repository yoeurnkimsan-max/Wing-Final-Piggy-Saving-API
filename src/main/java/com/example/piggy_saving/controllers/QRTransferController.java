package com.example.piggy_saving.controllers;


import com.example.piggy_saving.dto.request.QRPaymentPayload;
import com.example.piggy_saving.dto.request.QRTransferPayloadRequestDto;
import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.mappers.QRTransferMapper;
import com.example.piggy_saving.models.enums.TransferType;
import com.example.piggy_saving.services.TransferService;
import com.example.piggy_saving.util.DecodeBase64ToObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(QRTransferController.BASE_ROUTE)
@RequiredArgsConstructor
public class QRTransferController {
    public static final String BASE_ROUTE = "/api/v1/qr-transfers";

    private final DecodeBase64ToObject decodeBase64ToObject;
    private final TransferService transferService;
    private final QRTransferMapper qrTransferMapper;

    @PostMapping("/process")
    public TransferToPiggyRequestDto generateQRPayment(@RequestBody QRTransferPayloadRequestDto payload) {


        QRPaymentPayload requestPayload =
                decodeBase64ToObject.decodeBase64ToObject(payload.getQrBase64String(), QRPaymentPayload.class);

        TransferToPiggyRequestDto transfer = qrTransferMapper.toTransferToPiggyRequestDto(requestPayload, payload.getAmount());

        return transfer;
//        switch (requestPayload.getType()){
//            case TransferType.OWN: {
//
//                return "Own";
//            }
//            case TransferType.CONTRIBUTION:
//                return "Contribution";
//            case TransferType.P2P:
//                return "P2P";
//            default:
//                return "Unknown";
//        }
    }
}
