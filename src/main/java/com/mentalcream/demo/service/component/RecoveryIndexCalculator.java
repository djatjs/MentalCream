package com.mentalcream.demo.service.component;

import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
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

    private final DailyLogRepository dailyLogRepository;
    private final DoneItemRepository doneItemRepository;

    public int calculateIndex(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<DailyLog> logs = dailyLogRepository.findByLogDateBetween(weekStart, weekEnd);
        List<DoneItem> items = doneItemRepository.findByDailyLog_LogDateBetween(weekStart, weekEnd);

        // 1. 활동 질 점수 (카테고리 가중치)
        double qualityScore = items.stream().mapToDouble(item -> switch (item.getCategory()) {
            case PROJECT -> 3.0;
            case STUDY -> 2.0;
            case RUN -> 1.5;
            case LIFE -> 1.0;
        }).sum();

        // 2. 에너지 추세 분석 (전반부 vs 후반부)
        double firstHalfAvg = logs.stream()
                .filter(l -> !l.getLogDate().isAfter(weekStart.plusDays(2)))
                .mapToInt(DailyLog::getEnergy)
                .average().orElse(5.0);
        
        double secondHalfAvg = logs.stream()
                .filter(l -> l.getLogDate().isAfter(weekStart.plusDays(2)))
                .mapToInt(DailyLog::getEnergy)
                .average().orElse(firstHalfAvg);

        double trendBonus = (secondHalfAvg >= firstHalfAvg) ? 15 : 0;
        
        // 3. 최종 점수 산출
        int baseScore = (int) (qualityScore * 3);
        int energyScore = (int) (secondHalfAvg * 12); // 5점 만점 기준 (최대 60점)
        long penalty = (7 - logs.size()) * 12; // 미기록 페널티 강화

        int index = (int) (baseScore + energyScore + trendBonus - penalty);
        return Math.max(0, Math.min(100, index));
    }

    public String getStatus(int score) {
        if (score >= 70) return "흐름 유지 중";
        if (score >= 40) return "안정 유지 중";
        return "회복 필요";
    }
}
