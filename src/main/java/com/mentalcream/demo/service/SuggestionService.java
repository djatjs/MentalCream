package com.mentalcream.demo.service;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.domain.Suggestion;
import com.mentalcream.demo.domain.UserAccount;
import com.mentalcream.demo.dto.SuggestionDto;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
import com.mentalcream.demo.repository.SuggestionRepository;
import com.mentalcream.demo.service.component.EnergyPatternAnalyzer;
import com.mentalcream.demo.service.component.SuggestionPolicyEngine;
import com.mentalcream.demo.service.component.ZeroDayHandler;
import com.mentalcream.demo.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mental Cream v2 Suggestion Engine
 * 개인화된 에너지 패턴 분석 및 회복 중심 추천을 총괄함.
 */
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final DailyLogRepository dailyLogRepository;
    private final DoneItemRepository doneItemRepository;
    private final SuggestionRepository suggestionRepository;

    private final EnergyPatternAnalyzer analyzer;
    private final ZeroDayHandler zeroDayHandler;
    private final SuggestionPolicyEngine policyEngine;

    private final GeminiAiService geminiAiService;
    private final CurrentUserService currentUserService;

    @Transactional
    public SuggestionDto generateSuggestion(LocalDate forDate) {
        UserAccount user = currentUserService.requireUser();
        LocalDate yesterday = forDate.minusDays(1);
        DailyLog yesterdayLog = dailyLogRepository.findByUser_IdAndLogDate(user.getId(), yesterday).orElse(null);
        List<DoneItem> yesterdayItems = doneItemRepository.findByDailyLog_User_IdAndDailyLog_LogDateBetween(user.getId(), yesterday, yesterday);

        // 1. Zero Day 체크
        if (yesterdayItems.isEmpty()) {
            Suggestion recovery = zeroDayHandler.createRecoverySuggestion(user, forDate);
            return saveAndConvert(recovery);
        }

        // 2. 에너지 기반 카테고리 우선순위 결정
        double energyAvg = calculateRecentEnergyAvg(user.getId(), yesterday);
        Category targetCategory;

        if (energyAvg <= 2) {
            // 에너지가 매우 낮을 때는 강제로 LIFE(일상/회복) 카테고리로 고정하여 AI가 무리한 제안을 하지 않도록 함
            targetCategory = Category.LIFE;
        } else {
            // 3. 개인화 패턴 분석
            targetCategory = analyzer.findBestEnergyBoostingCategory(user.getId(), yesterday);
            if (targetCategory == null) targetCategory = findMostFrequentCategory(user.getId(), yesterday);
        }

        // 4. 추천 반복 제한 정책
        targetCategory = policyEngine.filterRepetition(user.getId(), targetCategory, forDate);

        // 5. AI를 통한 개인화 문구 생성 (핵심 변경 포인트)
        Map<String, String> aiResult = geminiAiService.generatePersonalizedSuggestion(
            targetCategory, 
            yesterdayLog != null ? yesterdayLog : DailyLog.builder().mood(3).energy(3).build(), 
            yesterdayItems
        );

        Suggestion suggestion = Suggestion.builder()
                .user(user)
                .logDate(forDate)
                .category(targetCategory)
                .title(aiResult.get("title"))
                .minutes(analyzer.getAdjustedMinutes(20, yesterdayLog != null ? yesterdayLog.getWorryIntensity() : 0))
                .reason(aiResult.get("reason"))
                .confidenceScore(analyzer.calculateConfidenceScore(user.getId(), yesterday))
                .build();

        return saveAndConvert(suggestion);
    }

    private double calculateRecentEnergyAvg(Long userId, LocalDate today) {
        return dailyLogRepository.findByUser_IdAndLogDateBetween(userId, today.minusDays(2), today).stream()
                .filter(l -> l.getEnergy() != null)
                .mapToInt(DailyLog::getEnergy)
                .average().orElse(3.0);
    }

    private Category findMostFrequentCategory(Long userId, LocalDate today) {
        List<DoneItem> items = doneItemRepository.findByDailyLog_User_IdAndDailyLog_LogDateBetween(userId, today.minusDays(2), today);
        return items.stream()
                .collect(Collectors.groupingBy(DoneItem::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Category.LIFE);
    }

    private SuggestionDto saveAndConvert(Suggestion s) {
        Suggestion target = suggestionRepository
                .findByUser_IdAndLogDate(s.getUser().getId(), s.getLogDate())
                .map(existing -> {
                    existing.setCategory(s.getCategory());
                    existing.setTitle(s.getTitle());
                    existing.setMinutes(s.getMinutes());
                    existing.setReason(s.getReason());
                    existing.setRecoveryFlag(s.getRecoveryFlag());
                    existing.setConfidenceScore(s.getConfidenceScore());
                    return existing;
                })
                .orElse(s);
        Suggestion saved = suggestionRepository.save(target);
        return SuggestionDto.builder()
                .id(saved.getId())
                .logDate(saved.getLogDate())
                .category(saved.getCategory().name())
                .title(saved.getTitle())
                .minutes(saved.getMinutes())
                .reason(saved.getReason())
                .confidenceScore(saved.getConfidenceScore())
                .build();
    }

}
