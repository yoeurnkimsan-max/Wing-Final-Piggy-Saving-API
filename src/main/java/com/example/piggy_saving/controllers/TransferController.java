package com.example.piggy_saving.controllers;

import com.example.piggy_saving.dto.request.*;
import com.example.piggy_saving.dto.response.*;
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
            @Valid @RequestBody TransferRequestDto transferRequestDto) {

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
    }

    /**
     * Transfer P2P
     */
    @PostMapping("/p2p")
    public ResponseEntity<ApiResponse<TransferP2PResponseDto>> transferPeerToPeer(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody TransferRequestDto transferRequestDto
    ) {
        TransferP2PResponseDto transferP2PRequestDto = transferService.transferP2P(customUserDetails.getUserId(), transferRequestDto);
        ApiResponse<TransferP2PResponseDto> apiResponse = ApiResponse.<TransferP2PResponseDto>builder()
                .success(true)
                .message("Transfer successfully")
                .statusCode(200)
                .statusMessage("OK")
                .data(transferP2PRequestDto)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Transfer Contribute
     */
    @PostMapping("/contribute")
    public ResponseEntity<ApiResponse<TransferContributeResponseDto>> transferToPiggyController(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody TransferRequestDto transferRequestDto
    ) {

        TransferContributeResponseDto transferResponse = transferService.transferContribute(customUserDetails.getUserId(), transferRequestDto);

        ApiResponse<TransferContributeResponseDto> apiResponse = ApiResponse.<TransferContributeResponseDto>builder()
                .success(true)
                .message("Transfer successfully")
                .statusCode(200)
                .statusMessage("OK")
                .data(transferResponse)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/break-piggy")
    public ResponseEntity<ApiResponse<TransferBreakPiggyResponseDto>> breakPiggy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TransferBreakRequestDto transferBreakRequestDto
    ) {

        TransferBreakPiggyResponseDto transferBreakPiggyService = transferService.transferBreak(userDetails.getUserId(), transferBreakRequestDto);
        ApiResponse<TransferBreakPiggyResponseDto> apiResponse = ApiResponse.<TransferBreakPiggyResponseDto>builder()
                .success(true)
                .message("Transfer successfully")
                .statusCode(200)
                .statusMessage("OK")
                .data(transferBreakPiggyService)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
