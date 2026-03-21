package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.request.QRPaymentPayload;
import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.models.enums.TransferType;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface QRTransferMapper {

    default TransferToPiggyRequestDto toTransferToPiggyRequestDto(QRPaymentPayload payload, BigDecimal amount) {

        if (payload == null) return null;

        String piggyNumber;

        // Decide which field to use based on transfer type
        switch (payload.getType()) {
            case TransferType.OWN:
                piggyNumber = payload.getAccountPiggyNumber();
                break;

            case TransferType.CONTRIBUTION:
                piggyNumber = payload.getPiggyAccountNumber();
                break;

            default:
                throw new IllegalArgumentException("Invalid type for piggy transfer");
        }

        return TransferToPiggyRequestDto.builder()
                .accountPiggyNumber(piggyNumber)
                .transferAmount(amount)
                .build();
    }
}