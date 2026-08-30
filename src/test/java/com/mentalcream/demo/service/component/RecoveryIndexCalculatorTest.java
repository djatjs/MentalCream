package com.mentalcream.demo.service.component;

import com.mentalcream.demo.repository.StatsMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveryIndexCalculatorTest {

    @Mock private StatsMapper statsMapper;
    @InjectMocks private RecoveryIndexCalculator calculator;

    @Test
    @DisplayName("현재 사용자의 집계값과 미기록 일수를 반영해 회복 지수를 계산한다")
    void shouldCalculateRecoveryIndexCorrectly() {
        Long userId = 7L;
        LocalDate start = LocalDate.of(2026, 2, 9);
        LocalDate end = start.plusDays(6);
        when(statsMapper.calculateRecoveryMetrics(userId, start, end)).thenReturn(Map.of(
                "QUALITY_SCORE", 10.0,
                "FIRST_HALF_AVG", 3.0,
                "SECOND_HALF_AVG", 3.0,
                "LOG_COUNT", 3L
        ));

        int score = calculator.calculateIndex(userId, start);

        assertThat(score).isEqualTo(33);
        assertThat(calculator.getStatus(score)).isEqualTo("회복 필요");
    }
}
