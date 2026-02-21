package com.mentalcream.demo.dto;

import com.mentalcream.demo.domain.DailyLog;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyLogDto {
    private java.time.LocalDate logDate;
    private Integer mood;
    private Integer energy;
    private String worryText;
    private Integer worryIntensity;
    private String note;

    public static DailyLogDto fromEntity(DailyLog entity) {
        return DailyLogDto.builder()
                .logDate(entity.getLogDate())
                .mood(entity.getMood())
                .energy(entity.getEnergy())
                .worryText(entity.getWorryText())
                .worryIntensity(entity.getWorryIntensity())
                .note(entity.getNote())
                .build();
    }
}
