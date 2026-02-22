package com.mentalcream.demo.service.component;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.repository.DailyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 행동 -> 에너지 변화 및 고민 감소 패턴을 분석하는 엔진 (v3)
 */
@Component
@RequiredArgsConstructor
public class EnergyPatternAnalyzer {

    private final DailyLogRepository dailyLogRepository;
    private final com.mentalcream.demo.repository.StatsMapper statsMapper;

    /**
     * 최근 14일 데이터를 분석하여 다음날 에너지 상승 효과가 가장 컸던 카테고리 추출
     */
    public Category findBestEnergyBoostingCategory(LocalDate today) {
        LocalDate startDate = today.minusDays(14);
        String bestCategory = statsMapper.findBestEnergyBoostingCategory(startDate, today);
        return bestCategory != null ? Category.valueOf(bestCategory) : null;
    }

    /**
     * 최근 14일간의 고민 감소율 평균 산출 (Oracle SQL 윈도우 함수 활용)
     */
    public double calculateWorryReductionRate(LocalDate today) {
        LocalDate startDate = today.minusDays(14);
        Double rate = statsMapper.calculateWorryReductionRate(startDate, today);
        return rate != null ? rate : 0.0;
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
