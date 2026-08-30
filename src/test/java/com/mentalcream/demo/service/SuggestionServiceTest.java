package com.mentalcream.demo.service;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.DoneItem;
import com.mentalcream.demo.domain.Suggestion;
import com.mentalcream.demo.domain.UserAccount;
import com.mentalcream.demo.dto.SuggestionDto;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
import com.mentalcream.demo.repository.SuggestionRepository;
import com.mentalcream.demo.security.CurrentUserService;
import com.mentalcream.demo.service.component.EnergyPatternAnalyzer;
import com.mentalcream.demo.service.component.SuggestionPolicyEngine;
import com.mentalcream.demo.service.component.ZeroDayHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock private DailyLogRepository dailyLogRepository;
    @Mock private DoneItemRepository doneItemRepository;
    @Mock private SuggestionRepository suggestionRepository;
    @Mock private EnergyPatternAnalyzer analyzer;
    @Mock private SuggestionPolicyEngine policyEngine;
    @Mock private ZeroDayHandler zeroDayHandler;
    @Mock private GeminiAiService geminiAiService;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks private SuggestionService suggestionService;

    private final UserAccount user = UserAccount.builder().id(7L).username("tester").build();
    private final LocalDate today = LocalDate.of(2026, 2, 12);
    private final LocalDate tomorrow = today.plusDays(1);

    @Test
    @DisplayName("현재 사용자의 활동이 없는 날에는 회복 추천을 생성한다")
    void shouldGenerateRecoverySuggestionOnZeroDay() {
        when(currentUserService.requireUser()).thenReturn(user);
        when(doneItemRepository.findByDailyLog_User_IdAndDailyLog_LogDateBetween(7L, today, today))
                .thenReturn(List.of());
        when(zeroDayHandler.createRecoverySuggestion(user, tomorrow)).thenReturn(
                Suggestion.builder().user(user).logDate(tomorrow).category(Category.LIFE)
                        .title("5분 리셋").recoveryFlag(true).build()
        );
        when(suggestionRepository.findByUser_IdAndLogDate(7L, tomorrow)).thenReturn(Optional.empty());
        when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SuggestionDto result = suggestionService.generateSuggestion(tomorrow);

        assertThat(result.getCategory()).isEqualTo("LIFE");
        assertThat(result.getTitle()).isEqualTo("5분 리셋");
    }

    @Test
    @DisplayName("현재 사용자의 에너지 상승 카테고리를 AI 추천에 반영한다")
    void shouldRecommendBestEnergyBoostingCategory() {
        DoneItem completed = DoneItem.builder().category(Category.STUDY).title("복습").build();
        when(currentUserService.requireUser()).thenReturn(user);
        when(doneItemRepository.findByDailyLog_User_IdAndDailyLog_LogDateBetween(7L, today, today))
                .thenReturn(List.of(completed));
        when(dailyLogRepository.findByUser_IdAndLogDateBetween(7L, today.minusDays(2), today))
                .thenReturn(List.of());
        when(analyzer.findBestEnergyBoostingCategory(7L, today)).thenReturn(Category.STUDY);
        when(policyEngine.filterRepetition(7L, Category.STUDY, tomorrow)).thenReturn(Category.STUDY);
        when(geminiAiService.generatePersonalizedSuggestion(any(), any(), any()))
                .thenReturn(Map.of("title", "복습 10분", "reason", "최근 학습 흐름을 이어가요."));
        when(analyzer.calculateConfidenceScore(7L, today)).thenReturn(80);
        when(suggestionRepository.findByUser_IdAndLogDate(7L, tomorrow)).thenReturn(Optional.empty());
        when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SuggestionDto result = suggestionService.generateSuggestion(tomorrow);

        assertThat(result.getCategory()).isEqualTo("STUDY");
        assertThat(result.getTitle()).isEqualTo("복습 10분");
        assertThat(result.getReason()).contains("학습 흐름");
    }
}
