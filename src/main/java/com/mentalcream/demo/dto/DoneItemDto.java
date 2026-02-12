package com.mentalcream.demo.dto;

import com.mentalcream.demo.domain.DoneItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoneItemDto {
    private Long id;
    private String category;
    private String title;
    private Integer minutes;
    private LocalDateTime createdAt;

    public static DoneItemDto fromEntity(DoneItem entity) {
        return DoneItemDto.builder()
                .id(entity.getId())
                .category(entity.getCategory().name())
                .title(entity.getTitle())
                .minutes(entity.getMinutes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
