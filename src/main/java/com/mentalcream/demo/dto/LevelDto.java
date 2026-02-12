package com.mentalcream.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelDto {
    private int level;
    private String levelName;
    private long currentXp;
    private long prevLevelXp;
    private long nextLevelXp;
    private double progressPercent;
}
