package com.mentalcream.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class WeeklyStatsResponse {
    private long totalDoneCount;
    private Map<String, Long> categoryCount;
    private double avgMood;
    private double avgEnergy;
    private List<DailyTrend> trends;

    @Getter
    @Builder
    public static class DailyTrend {
        private LocalDate date;
        private Integer energy;
        private Long doneCount;
    }
}
