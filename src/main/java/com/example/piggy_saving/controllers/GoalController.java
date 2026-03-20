package com.example.piggy_saving.controllers;


import com.example.piggy_saving.dto.response.PiggyGoalDetailedResponseDto;
import com.example.piggy_saving.dto.response.PiggyGoalResponseDto;
import com.example.piggy_saving.security.CustomUserDetails;
import com.example.piggy_saving.services.GoalService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(GoalController.BASE_ROUTE)
@AllArgsConstructor
public class GoalController {
    public static final String BASE_ROUTE = "/api/v1/piggy-goal";

    private final GoalService goalService;

    @GetMapping("/my-goals")
    public List<PiggyGoalDetailedResponseDto> findAllMyGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        return goalService.findAllMyGoals(userDetails.getUserId());
    }
}
