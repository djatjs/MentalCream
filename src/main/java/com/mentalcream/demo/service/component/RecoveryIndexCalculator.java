package com.mentalcream.demo.service.component;

import com.mentalcream.demo.repository.StatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 7일 데이터를 기반으로 0~100점의 회복 지수를 산출하는 엔진
 */
@Component
@RequiredArgsConstructor
public class RecoveryIndexCalculator {

    private final com.mentalcream.demo.repository.StatsMapper statsMapper;

    public int calculateIndex(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        
        // MyBatis + Oracle Native SQL을 통한 집계 처리 (SM 현업 스타일)
        java.util.Map<String, Object> metrics = statsMapper.calculateRecoveryMetrics(weekStart, weekEnd);
        
        double qualityScore = ((Number) metrics.getOrDefault("QUALITY_SCORE", 0.0)).doubleValue();
        double firstHalfAvg = ((Number) metrics.getOrDefault("FIRST_HALF_AVG", 5.0)).doubleValue();
        double secondHalfAvg = ((Number) metrics.getOrDefault("SECOND_HALF_AVG", firstHalfAvg)).doubleValue();
        long logCount = ((Number) metrics.getOrDefault("LOG_COUNT", 0L)).longValue();

        // 2. 에너지 추세 분석 (전반부 vs 후반부)
        double trendBonus = (secondHalfAvg >= firstHalfAvg) ? 15 : 0;
        
        // 3. 최종 점수 산출
        int baseScore = (int) (qualityScore * 3);
        int energyScore = (int) (secondHalfAvg * 12); // 5점 만점 기준 (최대 60점)
        long penalty = (7 - logCount) * 12; // 미기록 페널티 강화

        int index = (int) (baseScore + energyScore + trendBonus - penalty);
        return Math.max(0, Math.min(100, index));
    }

    public String getStatus(int score) {
        if (score >= 70) return "흐름 유지 중";
        if (score >= 40) return "안정 유지 중";
        return "회복 필요";
    }
}
