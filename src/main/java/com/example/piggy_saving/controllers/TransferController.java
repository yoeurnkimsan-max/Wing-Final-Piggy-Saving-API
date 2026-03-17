package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.TransferToPiggyRequestDto;
import com.example.piggy_saving.dto.response.ApiResponse;
import com.example.piggy_saving.dto.response.TransferResponseDto;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.TransferService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping(TransferController.BASE_ROUTE)
@AllArgsConstructor
public class TransferController {
    public static final String BASE_ROUTE = "/api/v1/transfers";


    private final TransferService transferService;
//    private final TransferService transferService;

    /**
     * Transfer Money to Piggy Goal
     */
    @PostMapping("/main-to-piggy")
    public ResponseEntity<ApiResponse<TransferResponseDto>> transferToPiggy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TransferToPiggyRequestDto transferRequestDto) {

        TransferResponseDto response = transferService.transferToPiggy(userDetails.getUserId(), transferRequestDto);
        ApiResponse<TransferResponseDto> apiResponse = ApiResponse.<TransferResponseDto>builder()
                .success(true)
                .message("Transfer successfully")
                .statusCode(200)
                .statusMessage("OK")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
//        return null;
    }
}
