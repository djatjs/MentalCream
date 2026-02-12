package com.mentalcream.demo.dto.response;

import com.mentalcream.demo.dto.LevelDto;
import com.mentalcream.demo.dto.DailyLogDto;
import com.mentalcream.demo.dto.DoneItemDto;
import com.mentalcream.demo.dto.SuggestionDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TodayResponse {
    private LocalDate todayDate;
    private DailyLogDto dailyLog;
    private List<DoneItemDto> doneItems;
    private SuggestionDto tomorrowSuggestion;
    private LevelDto levelInfo;
    private String mentalMode;
    private double worryReductionRate; // 고민 감소율 추가
    private int streakDays; // 연속 기록 일수 추가
}
