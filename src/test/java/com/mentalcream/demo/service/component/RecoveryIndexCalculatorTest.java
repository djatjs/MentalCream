package com.mentalcream.demo.service.component;

import com.mentalcream.demo.domain.DailyLog;
import com.mentalcream.demo.repository.DailyLogRepository;
import com.mentalcream.demo.repository.DoneItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveryIndexCalculatorTest {

    @Mock private DailyLogRepository dailyLogRepository;
    @Mock private DoneItemRepository doneItemRepository;

    @InjectMocks private RecoveryIndexCalculator calculator;

    @Test
    @DisplayName("회복 지수 계산 로직이 정확해야 한다 (기록 부족 시 패널티 적용)")
    void shouldCalculateRecoveryIndexCorrectly() {
        // given
        LocalDate start = LocalDate.of(2026, 2, 9);
        LocalDate end = start.plusDays(6);
        
        // 7일 중 3일만 기록 (4일 공백 -> 40점 감점)
        when(dailyLogRepository.findByLogDateBetween(start, end)).thenReturn(List.of(
                DailyLog.builder().energy(3).build(),
                DailyLog.builder().energy(3).build(),
                DailyLog.builder().energy(3).build()
        ));
        // 활동 2개 (10점 가산)
        when(doneItemRepository.findByDailyLog_LogDateBetween(start, end)).thenReturn(List.of(any(), any()));
        
        // Expected Score: (2*5) + (3*10) - (4*10) = 10 + 30 - 40 = 0
        
        // when
        int score = calculator.calculateIndex(start);
        String status = calculator.getStatus(score);

        // then
        assertThat(score).isEqualTo(0);
        assertThat(status).isEqualTo("회복 필요");
    }
    
    private <T> T any() { return null; }
}
