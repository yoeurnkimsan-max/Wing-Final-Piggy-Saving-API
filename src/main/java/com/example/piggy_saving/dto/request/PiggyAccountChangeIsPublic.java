package com.example.piggy_saving.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PiggyAccountChangeIsPublic {

    @NotNull
    @JsonProperty("is_public")
    boolean changePiggyToIsPublic;
}
