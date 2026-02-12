package com.mentalcream.demo.service.component;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 행동 -> 에너지 변화 및 고민 감소 패턴을 분석하는 엔진 (v3)
 */
@Component
@RequiredArgsConstructor
public class EnergyPatternAnalyzer {

    private final DailyLogRepository dailyLogRepository;
    private final DoneItemRepository doneItemRepository;

    /**
     * 최근 14일 데이터를 분석하여 다음날 에너지 상승 효과가 가장 컸던 카테고리 추출
     */
    public Category findBestEnergyBoostingCategory(LocalDate today) {
        LocalDate fourteenDaysAgo = today.minusDays(14);
        List<DailyLog> logs = dailyLogRepository.findByLogDateBetween(fourteenDaysAgo, today);
        List<DoneItem> items = doneItemRepository.findByDailyLog_LogDateBetween(fourteenDaysAgo, today);

        Map<LocalDate, Integer> energyMap = logs.stream()
                .filter(l -> l.getEnergy() != null)
                .collect(Collectors.toMap(DailyLog::getLogDate, DailyLog::getEnergy, (v1, v2) -> v1));

        Map<Category, List<Double>> categoryBoosts = new EnumMap<>(Category.class);

        for (DoneItem item : items) {
            LocalDate itemDate = item.getDailyLog().getLogDate();
            LocalDate nextDate = itemDate.plusDays(1);

            if (energyMap.containsKey(itemDate) && energyMap.containsKey(nextDate)) {
                double delta = energyMap.get(nextDate) - energyMap.get(itemDate);
                categoryBoosts.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(delta);
            }
        }

        return categoryBoosts.entrySet().stream()
                .max(Comparator.comparingDouble(entry -> entry.getValue().stream().mapToDouble(d -> d).average().orElse(-9.9)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public double calculateWorryReductionRate(LocalDate today) {
        LocalDate fourteenDaysAgo = today.minusDays(14);
        List<DailyLog> logs = dailyLogRepository.findByLogDateBetween(fourteenDaysAgo, today);
        
        if (logs.size() < 2) return 0.0;

        logs.sort(Comparator.comparing(DailyLog::getLogDate));

        double totalDelta = 0;
        int pairCount = 0;

        for (int i = 0; i < logs.size() - 1; i++) {
            DailyLog current = logs.get(i);
            DailyLog next = logs.get(i + 1);
            
            if (current.getLogDate().plusDays(1).equals(next.getLogDate())) {
                // null인 경우 0으로 취급하여 계산에 포함
                int currentWorry = current.getWorryIntensity() != null ? current.getWorryIntensity() : 0;
                int nextWorry = next.getWorryIntensity() != null ? next.getWorryIntensity() : 0;
                
                totalDelta += (currentWorry - nextWorry);
                pairCount++;
            }
        }
        return pairCount == 0 ? 0.0 : totalDelta / pairCount;
    }

    /**
     * 고민 강도에 따른 활동 시간 가중치 조절
     */
    public int getAdjustedMinutes(int baseMinutes, int worryIntensity) {
        if (worryIntensity >= 4) return 15; // 고민이 많으면 가볍게
        return baseMinutes;
    }

    /**
     * 데이터 신뢰도 산출
     */
    public int calculateConfidenceScore(LocalDate today) {
        LocalDate fourteenDaysAgo = today.minusDays(14);
        long recordCount = dailyLogRepository.findByLogDateBetween(fourteenDaysAgo, today).size();
        if (recordCount >= 10) return 85;
        if (recordCount >= 5) return 60;
        return 40;
    }
}
