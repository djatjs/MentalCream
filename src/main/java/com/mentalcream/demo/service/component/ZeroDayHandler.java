package com.mentalcream.demo.service.component;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.Suggestion;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 활동 공백(Zero Day) 발생 시 즉각적인 회복 처방을 생성하는 핸들러
 */
@Component
public class ZeroDayHandler {

    public Suggestion createRecoverySuggestion(LocalDate forDate) {
        return Suggestion.builder()
                .logDate(forDate)
                .category(Category.LIFE)
                .title("5분 리셋 액션 (스트레칭 + 물 한 컵)")
                .minutes(5)
                .reason("공백을 0으로 두지 않기 위한 최소 행동")
                .recoveryFlag(true)
                .confidenceScore(100)
                .build();
    }
}
