package com.mentalcream.demo.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface StatsMapper {

    // Oracle 최적화 SQL을 사용하여 7일간의 지표를 단일 쿼리로 집계
    Map<String, Object> calculateRecoveryMetrics(@Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    // 전체 활동 데이터를 기반으로 누적 경험치(XP) 산출
    long calculateTotalXp();

    // 연속 기록(Streak) 일수 산출
    int calculateCurrentStreak(@Param("today") LocalDate today);
}
