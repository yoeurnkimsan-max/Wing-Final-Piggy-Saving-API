package com.example.piggy_saving.dto.response;

import com.example.piggy_saving.models.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QRContributeDataResponse {
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("goal_name")
    private String goalName;
    private TransferType type;
}
