package com.mentalcream.demo.service;

import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.dto.response.WeeklyStatsResponse;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final DailyLogRepository dailyLogRepository;
    private final DoneItemRepository doneItemRepository;

    public WeeklyStatsResponse getWeeklyStats(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);

        List<DailyLog> logs = dailyLogRepository.findByLogDateBetween(weekStart, weekEnd);
        List<DoneItem> items = doneItemRepository.findByDailyLog_LogDateBetween(weekStart, weekEnd);

        long totalDoneCount = items.size();

        Map<String, Long> categoryCount = items.stream()
                .collect(Collectors.groupingBy(item -> item.getCategory().name(), Collectors.counting()));

        double avgMood = logs.stream()
                .filter(log -> log.getMood() != null)
                .mapToInt(DailyLog::getMood)
                .average()
                .orElse(0.0);

        double avgEnergy = logs.stream()
                .filter(log -> log.getEnergy() != null)
                .mapToInt(DailyLog::getEnergy)
                .average()
                .orElse(0.0);

        List<WeeklyStatsResponse.DailyTrend> trends = weekStart.datesUntil(weekEnd.plusDays(1))
                .map(date -> {
                    Integer energy = logs.stream()
                            .filter(l -> l.getLogDate().equals(date))
                            .map(DailyLog::getEnergy)
                            .findFirst().orElse(null);
                    long doneCount = items.stream()
                            .filter(i -> i.getDailyLog().getLogDate().equals(date))
                            .count();
                    return WeeklyStatsResponse.DailyTrend.builder()
                            .date(date)
                            .energy(energy)
                            .doneCount(doneCount)
                            .build();
                }).collect(Collectors.toList());

        return WeeklyStatsResponse.builder()
                .totalDoneCount(totalDoneCount)
                .categoryCount(categoryCount)
                .avgMood(avgMood)
                .avgEnergy(avgEnergy)
                .trends(trends)
                .build();
    }
}
