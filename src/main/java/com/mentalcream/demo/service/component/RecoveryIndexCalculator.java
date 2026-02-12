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

        long totalDoneCount = items.size();
        double avgEnergy = logs.stream()
                .filter(l -> l.getEnergy() != null)
                .mapToInt(DailyLog::getEnergy)
                .average().orElse(0.0);
        
        long activeDays = logs.size();
        long zeroDayCount = 7 - activeDays; // 기록이 없는 날 포함

        int baseScore = (int) (totalDoneCount * 5);
        int energyScore = (int) (avgEnergy * 10);
        int penalty = (int) (zeroDayCount * 10);

        int index = baseScore + energyScore - penalty;
        return Math.max(0, Math.min(100, index));
    }

    public String getStatus(int score) {
        if (score >= 70) return "흐름 유지 중";
        if (score >= 40) return "안정 유지 중";
        return "회복 필요";
    }
}
