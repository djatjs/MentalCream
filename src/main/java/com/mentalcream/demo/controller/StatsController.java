package com.mentalcream.demo.controller;

import com.mentalcream.demo.dto.response.RecoveryIndexResponse;
import com.mentalcream.demo.dto.response.WeeklyStatsResponse;
import com.mentalcream.demo.service.StatsService;
import com.mentalcream.demo.service.component.RecoveryIndexCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final RecoveryIndexCalculator recoveryIndexCalculator;

    @GetMapping("/weekly")
    public WeeklyStatsResponse getWeeklyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return statsService.getWeeklyStats(weekStart);
    }

    @GetMapping("/recovery-index")
    public RecoveryIndexResponse getRecoveryIndex(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        int score = recoveryIndexCalculator.calculateIndex(weekStart);
        return RecoveryIndexResponse.builder()
                .score(score)
                .status(recoveryIndexCalculator.getStatus(score))
                .build();
    }
}
