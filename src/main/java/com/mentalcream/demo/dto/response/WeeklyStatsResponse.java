package com.mentalcream.demo.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class WeeklyStatsResponse {
    private long totalDoneCount;
    private Map<String, Long> categoryCount;
    private double avgMood;
    private double avgEnergy;
}
