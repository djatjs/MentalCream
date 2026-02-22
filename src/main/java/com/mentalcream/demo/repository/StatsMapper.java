package com.mentalcream.demo.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface StatsMapper {

    // Oracle 최적화 SQL을 사용하여 7일간의 지표를 단일 쿼리로 집계
    Map<String, Object> calculateRecoveryMetrics(@Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    // 전체 활동 데이터를 기반으로 누적 경험치(XP) 산출.
    long calculateTotalXp();

    // 연속 기록(Streak) 일수 산출
    int calculateCurrentStreak(@Param("today") LocalDate today);

    // 최근 14일 분석: 에너지 상승 효과가 가장 컸던 활동 카테고리 추출
    String findBestEnergyBoostingCategory(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 최근 14일 분석: 일일 고민 강도 변화량 평균 산출 (Oracle LEAD 함수 활용)
    Double calculateWorryReductionRate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
