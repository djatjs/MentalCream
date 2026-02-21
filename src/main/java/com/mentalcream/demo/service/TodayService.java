package com.mentalcream.demo.service;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.domain.Suggestion;
import com.mentalcream.demo.dto.DailyLogDto;
import com.mentalcream.demo.dto.DoneItemDto;
import com.mentalcream.demo.dto.SuggestionDto;
import com.mentalcream.demo.dto.request.AddDoneItemRequest;
import com.mentalcream.demo.dto.request.UpdateDailyLogRequest;
import com.mentalcream.demo.dto.response.TodayResponse;
import com.mentalcream.demo.exception.ResourceNotFoundException;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
import com.mentalcream.demo.repository.SuggestionRepository;
import com.mentalcream.demo.service.component.EnergyPatternAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayService {

    private final DailyLogRepository dailyLogRepository;
    private final DoneItemRepository doneItemRepository;
    private final SuggestionRepository suggestionRepository;

    private final GamificationService gamificationService;

    private final EnergyPatternAnalyzer analyzer;

    public TodayResponse getTodayScreen(LocalDate date) {
        DailyLog dailyLog = dailyLogRepository.findByLogDate(date).orElse(null);
        List<DoneItem> doneItems = dailyLog != null ? dailyLog.getDoneItems() : List.of();
        Suggestion suggestion = suggestionRepository.findByLogDate(date.plusDays(1)).orElse(null);

        return TodayResponse.builder()
                .todayDate(date)
                .dailyLog(dailyLog != null ? DailyLogDto.fromEntity(dailyLog) : null)
                .doneItems(doneItems.stream().map(DoneItemDto::fromEntity).collect(Collectors.toList()))
                .tomorrowSuggestion(suggestion != null ? SuggestionDto.builder()
                        .id(suggestion.getId())
                        .logDate(suggestion.getLogDate())
                        .category(suggestion.getCategory().name())
                        .title(suggestion.getTitle())
                        .minutes(suggestion.getMinutes())
                        .reason(suggestion.getReason())
                        .confidenceScore(suggestion.getConfidenceScore())
                        .build() : null)
                .levelInfo(gamificationService.calculateUserLevel())
                .mentalMode(gamificationService.getMentalMode(date))
                .worryReductionRate(analyzer.calculateWorryReductionRate(date))
                .streakDays(gamificationService.calculateStreak(date))
                .build();
    }

    @Transactional
    public DailyLogDto upsertDailyLog(LocalDate date, UpdateDailyLogRequest request) {
        DailyLog dailyLog = dailyLogRepository.findByLogDate(date)
                .orElseGet(() -> DailyLog.builder().logDate(date).build());

        dailyLog.setMood(request.getMood());
        dailyLog.setEnergy(request.getEnergy());
        dailyLog.setNote(request.getNote());
        dailyLog.setWorryText(request.getWorryText());
        // 지시사항: 고민 기록 안 한 날은 자동으로 Zero Worry(0) 처리
        dailyLog.setWorryIntensity(request.getWorryIntensity() != null ? request.getWorryIntensity() : 0);

        DailyLog savedLog = dailyLogRepository.save(dailyLog);
        return DailyLogDto.fromEntity(savedLog);
    }

    @Transactional
    public DoneItemDto addDoneItem(LocalDate date, AddDoneItemRequest request) {
        DailyLog dailyLog = dailyLogRepository.findByLogDate(date)
                .orElseGet(() -> dailyLogRepository.save(DailyLog.builder().logDate(date).build()));

        DoneItem doneItem = DoneItem.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .minutes(request.getMinutes())
                .intensity(request.getIntensity())
                .build();

        dailyLog.addDoneItem(doneItem); // Set relationship

        DoneItem savedItem = doneItemRepository.save(doneItem);
        return DoneItemDto.fromEntity(savedItem);
    }

    @Transactional
    public void deleteDoneItem(Long doneItemId) {
        if (!doneItemRepository.existsById(doneItemId)) {
            throw new ResourceNotFoundException("DoneItem not found with id: " + doneItemId);
        }
        doneItemRepository.deleteById(doneItemId);
    }
}
