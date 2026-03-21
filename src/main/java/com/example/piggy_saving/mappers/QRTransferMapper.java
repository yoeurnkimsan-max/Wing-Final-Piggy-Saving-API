package com.example.piggy_saving.mappers;

import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;

public interface QRTransferMapper {
    TransferToPiggyRequestDto toTransferToPiggyRequestDto(TransferToPiggyRequestDto payload);
}
