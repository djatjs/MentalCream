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
import com.mentalcream.demo.service.component.RecoveryIndexCalculator;
import com.mentalcream.demo.service.component.SuggestionPolicyEngine;
import com.mentalcream.demo.service.component.ZeroDayHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    
    @InjectMocks private SuggestionService suggestionService;

    private final LocalDate today = LocalDate.of(2026, 2, 12);
    private final LocalDate tomorrow = today.plusDays(1);

    @Test
    @DisplayName("Zero Day인 경우 Recovery Suggestion이 생성되어야 한다")
    void shouldGenerateRecoverySuggestionOnZeroDay() {
        // given
        when(doneItemRepository.findByDailyLog_LogDateBetween(today, today)).thenReturn(new ArrayList<>());
        when(zeroDayHandler.createRecoverySuggestion(tomorrow)).thenReturn(
                Suggestion.builder().category(Category.LIFE).title("5분 리셋").recoveryFlag(true).build()
        );
        when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SuggestionDto result = suggestionService.generateSuggestion(tomorrow);

        // then
        assertThat(result.getCategory()).isEqualTo("LIFE");
        assertThat(result.getTitle()).isEqualTo("5분 리셋");
    }

    @Test
    @DisplayName("에너지 상승 효과가 가장 큰 카테고리가 1순위로 추천되어야 한다")
    void shouldRecommendBestEnergyBoostingCategory() {
        // given
        when(doneItemRepository.findByDailyLog_LogDateBetween(today, today)).thenReturn(List.of(any()));
        when(analyzer.findBestEnergyBoostingCategory(today)).thenReturn(Category.STUDY);
        when(policyEngine.filterRepetition(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(suggestionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // when
        SuggestionDto result = suggestionService.generateSuggestion(tomorrow);

        // then
        assertThat(result.getCategory()).isEqualTo("STUDY");
        assertThat(result.getReason()).contains("에너지를 높이는 패턴");
    }
}
