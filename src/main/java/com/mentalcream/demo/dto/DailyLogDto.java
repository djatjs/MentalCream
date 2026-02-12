package com.mentalcream.demo.dto;

import com.mentalcream.demo.domain.DailyLog;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyLogDto {
    private Integer mood;
    private Integer energy;
    private String worryText;
    private Integer worryIntensity;
    private String note;

    public static DailyLogDto fromEntity(DailyLog entity) {
        return DailyLogDto.builder()
                .mood(entity.getMood())
                .energy(entity.getEnergy())
                .worryText(entity.getWorryText())
                .worryIntensity(entity.getWorryIntensity())
                .note(entity.getNote())
                .build();
    }
}
