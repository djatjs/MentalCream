package com.mentalcream.demo.service;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.domain.Suggestion;
import com.mentalcream.demo.dto.SuggestionDto;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
import com.mentalcream.demo.repository.SuggestionRepository;
import com.mentalcream.demo.service.component.EnergyPatternAnalyzer;
import com.mentalcream.demo.service.component.SuggestionPolicyEngine;
import com.mentalcream.demo.service.component.ZeroDayHandler;
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

    @Transactional
    public SuggestionDto generateSuggestion(LocalDate forDate) {
        LocalDate yesterday = forDate.minusDays(1);
        DailyLog yesterdayLog = dailyLogRepository.findByLogDate(yesterday).orElse(null);
        List<DoneItem> yesterdayItems = doneItemRepository.findByDailyLog_LogDateBetween(yesterday, yesterday);

        // 1. Zero Day 체크
        if (yesterdayItems.isEmpty()) {
            Suggestion recovery = zeroDayHandler.createRecoverySuggestion(forDate);
            return saveAndConvert(recovery);
        }

        // 2. 에너지 기반 필터링
        double energyAvg = calculateRecentEnergyAvg(yesterday);
        if (energyAvg <= 2) {
            return saveAndConvert(createLifeSuggestionForLowEnergy(forDate));
        }

        // 3. 개인화 패턴 분석
        Category targetCategory = analyzer.findBestEnergyBoostingCategory(yesterday);
        if (targetCategory == null) targetCategory = findMostFrequentCategory(yesterday);

        // 4. 추천 반복 제한 정책
        targetCategory = policyEngine.filterRepetition(targetCategory, forDate);

        // 5. AI를 통한 개인화 문구 생성 (핵심 변경 포인트)
        Map<String, String> aiResult = geminiAiService.generatePersonalizedSuggestion(
            targetCategory, 
            yesterdayLog != null ? yesterdayLog : DailyLog.builder().mood(3).energy(3).build(), 
            yesterdayItems
        );

        Suggestion suggestion = Suggestion.builder()
                .logDate(forDate)
                .category(targetCategory)
                .title(aiResult.get("title"))
                .minutes(analyzer.getAdjustedMinutes(20, yesterdayLog != null ? yesterdayLog.getWorryIntensity() : 0))
                .reason(aiResult.get("reason"))
                .confidenceScore(analyzer.calculateConfidenceScore(yesterday))
                .build();

        return saveAndConvert(suggestion);
    }

    private double calculateRecentEnergyAvg(LocalDate today) {
        return dailyLogRepository.findByLogDateBetween(today.minusDays(2), today).stream()
                .filter(l -> l.getEnergy() != null)
                .mapToInt(DailyLog::getEnergy)
                .average().orElse(3.0);
    }

    private Category findMostFrequentCategory(LocalDate today) {
        List<DoneItem> items = doneItemRepository.findByDailyLog_LogDateBetween(today.minusDays(2), today);
        return items.stream()
                .collect(Collectors.groupingBy(DoneItem::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Category.LIFE);
    }

    private Suggestion buildSuggestion(LocalDate forDate, Category category) {
        String title = switch (category) {
            case STUDY -> "JOIN 실전 문제 5개 풀기";
            case RUN -> "3km 이지런 (실내 코어 15분)";
            case PROJECT -> "프로젝트 TODO 1개 끝내기";
            case LIFE -> "책 10페이지 읽기";
        };

        String reason = generateRandomCharacterReason(category);
        return Suggestion.builder().logDate(forDate).category(category).title(title).minutes(20).reason(reason).build();
    }

    private String generateRandomCharacterReason(Category category) {
        String[] tones = {
            "코치형: 페이스가 좋습니다. 이 흐름을 유지해서 실력을 굳혀보세요!",
            "친구형: 오늘도 고생 많았어. 내일은 딱 이것만 하고 푹 쉬자!",
            "냉소형: 불안해할 시간 있으면 이거 하나라도 더 끝내는 게 이득이야.",
            "RPG형: 새로운 퀘스트 도착! 이 행동을 완료하고 XP를 획득하세요."
        };
        int randomIndex = (int) (Math.random() * tones.length);
        return tones[randomIndex];
    }

    private SuggestionDto saveAndConvert(Suggestion s) {
        Suggestion saved = suggestionRepository.save(s);
        return SuggestionDto.builder()
                .category(saved.getCategory().name())
                .title(saved.getTitle())
                .minutes(saved.getMinutes())
                .reason(saved.getReason())
                .confidenceScore(saved.getConfidenceScore())
                .build();
    }

    private Suggestion createLifeSuggestionForLowEnergy(LocalDate forDate) {
        return Suggestion.builder()
                .logDate(forDate).category(Category.LIFE).title("스트레칭 10분 + 물 한 컵").minutes(10).reason("에너지가 낮을 땐 몸부터 리셋").confidenceScore(90).build();
    }
}
