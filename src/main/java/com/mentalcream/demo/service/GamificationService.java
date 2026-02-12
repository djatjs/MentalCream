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

    private final DoneItemRepository doneItemRepository;
    private final RecoveryIndexCalculator recoveryIndexCalculator;

    public LevelDto calculateUserLevel() {
        List<DoneItem> allItems = doneItemRepository.findAll();
        long totalXp = allItems.stream()
                .mapToLong(this::getXpByCategory)
                .sum();

        int level = 1;
        String levelName = "회복자";
        long nextLevelXp = 100;

        if (totalXp >= 1000) { level = 5; levelName = "멘탈관리자"; nextLevelXp = 2000; }
        else if (totalXp >= 600) { level = 4; levelName = "흐름장인"; nextLevelXp = 1000; }
        else if (totalXp >= 300) { level = 3; levelName = "전진자"; nextLevelXp = 600; }
        else if (totalXp >= 100) { level = 2; levelName = "유지자"; nextLevelXp = 300; }

        long prevLevelXp = getPrevLevelXp(level);
        double progress = (double) (totalXp - prevLevelXp) / (nextLevelXp - prevLevelXp) * 100;

        return LevelDto.builder()
                .level(level)
                .levelName(levelName)
                .currentXp(totalXp)
                .prevLevelXp(prevLevelXp)
                .nextLevelXp(nextLevelXp)
                .progressPercent(Math.min(100, progress))
                .build();
    }

    public String getMentalMode(LocalDate date) {
        int score = recoveryIndexCalculator.calculateIndex(date.minusDays(7));
        if (score >= 80) return "🔥 가속 모드";
        if (score >= 60) return "🔄 유지 모드";
        if (score >= 40) return "🌱 회복 모드";
        return "🧊 냉각 모드";
    }

    public int calculateStreak(LocalDate today) {
        int streak = 0;
        LocalDate checkDate = today;
        while (true) {
            if (doneItemRepository.findByDailyLog_LogDateBetween(checkDate, checkDate).isEmpty()) {
                if (checkDate.equals(today)) { // 오늘만 아직 안 적은 경우는 어제부터 체크
                    checkDate = checkDate.minusDays(1);
                    continue;
                }
                break;
            }
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
    }

    private int getXpByCategory(DoneItem item) {
        return switch (item.getCategory()) {
            case LIFE -> 5;
            case STUDY -> 10;
            case RUN -> 15;
            case PROJECT -> 20;
        };
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
