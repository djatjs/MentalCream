package com.mentalcream.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "done_item", indexes = @Index(name = "idx_log_date", columnList = "log_date"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoneItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_date", nullable = false)
    private DailyLog dailyLog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(length = 120, nullable = false)
    private String title;

    private Integer minutes;

    private Integer intensity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
