package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.request.QRPaymentPayload;
import com.example.piggy_saving.dto.request.TransferRequestDto;
import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.models.enums.TransferType;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface QRTransferMapper {

    default TransferRequestDto toTransferToPiggyRequestDto(QRPaymentPayload payload, BigDecimal amount) {

        if (payload == null) return null;



        return TransferRequestDto.builder()
//                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .build();
    }
}