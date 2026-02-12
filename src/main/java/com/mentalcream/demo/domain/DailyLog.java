package com.mentalcream.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLog {

    @Id
    @Column(name = "log_date")
    private LocalDate logDate;

    private Integer mood;

    private Integer energy;

    @Column(name = "worry_text", length = 200)
    private String worryText;

    @Column(name = "worry_intensity")
    @Builder.Default
    private Integer worryIntensity = 0;

    @Column(length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "dailyLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoneItem> doneItems = new ArrayList<>();

    public void addDoneItem(DoneItem item) {
        doneItems.add(item);
        item.setDailyLog(this);
    }
}
