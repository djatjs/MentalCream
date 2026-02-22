package com.mentalcream.demo.service;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.dto.LevelDto;
import com.mentalcream.demo.repository.DoneItemRepository;
import com.mentalcream.demo.service.component.RecoveryIndexCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final com.mentalcream.demo.repository.StatsMapper statsMapper;
    private final RecoveryIndexCalculator recoveryIndexCalculator;

    public LevelDto calculateUserLevel() {
        // MyBatis + Oracle SQL로 전체 XP 한 번에 계산 (사용자가 나뿐이기에 가능한 일)
        long totalXp = statsMapper.calculateTotalXp();

        int level = 1;
        String levelName = "회복자"; // 명칭 통일
        long nextLevelXp = 100;

        if (totalXp >= 1000) { level = 5; levelName = "멘탈관리자"; nextLevelXp = 2000; }
        else if (totalXp >= 600) { level = 4; levelName = "흐름장인"; nextLevelXp = 1000; }
        else if (totalXp >= 300) { level = 3; levelName = "전진자"; nextLevelXp = 600; }
        else if (totalXp >= 100) { level = 2; levelName = "유지자"; nextLevelXp = 300; }

        long prevLevelXp = getPrevLevelXp(level);
        double progress = (nextLevelXp == prevLevelXp) ? 100 : (double) (totalXp - prevLevelXp) / (nextLevelXp - prevLevelXp) * 100;

        return LevelDto.builder()
                .level(level)
                .levelName(levelName)
                .currentXp(totalXp)
                .prevLevelXp(prevLevelXp)
                .nextLevelXp(nextLevelXp)
                .progressPercent(Math.min(100, Math.max(0, progress)))
                .build();
    }

    public String getMentalMode(LocalDate date) {
        int score = recoveryIndexCalculator.calculateIndex(date.minusDays(7));
        return getMentalModeByScore(score);
    }

    public String getMentalModeByScore(int score) {
        if (score >= 80) return "🔥 가속 모드";
        if (score >= 60) return "🔄 유지 모드";
        if (score >= 40) return "🌱 회복 모드";
        return "🧊 냉각 모드";
    }

    public int calculateStreak(LocalDate today) {
        // MyBatis + 계층형 쿼리로 연속 기록 한 번에 계산
        return statsMapper.calculateCurrentStreak(today);
    }

    private long getPrevLevelXp(int level) {
        return switch (level) {
            case 5 -> 1000;
            case 4 -> 600;
            case 3 -> 300;
            case 2 -> 100;
            default -> 0;
        };
    }
}
