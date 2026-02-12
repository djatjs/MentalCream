package com.mentalcream.demo.service.component;

import com.mentalcream.demo.domain.Category;
import com.mentalcream.demo.domain.Suggestion;
import com.mentalcream.demo.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 중복 방지 및 최종 추천 카테고리를 결정하는 정책 엔진
 */
@Component
@RequiredArgsConstructor
public class SuggestionPolicyEngine {

    private final SuggestionRepository suggestionRepository;

    public Category filterRepetition(Category candidate, LocalDate today) {
        List<Suggestion> history = suggestionRepository.findTop3ByLogDateBeforeOrderByLogDateDesc(today);
        
        long repeatCount = history.stream()
                .filter(s -> s.getCategory() == candidate)
                .count();

        if (repeatCount >= 3) {
            // 3회 반복 시 다음 차선책(LIFE)으로 강제 전환
            return (candidate == Category.LIFE) ? Category.STUDY : Category.LIFE;
        }
        
        return candidate;
    }
}
