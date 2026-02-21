package com.mentalcream.demo.dto;

import com.mentalcream.demo.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuggestionDto {
    private Long id;
    private java.time.LocalDate logDate;
    private String category;
    private String title;
    private Integer minutes;
    private String reason;
    private Integer confidenceScore;
}
