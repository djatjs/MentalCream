package com.mentalcream.demo.repository;

import com.mentalcream.demo.domain.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    Optional<DailyLog> findByUser_IdAndLogDate(Long userId, LocalDate logDate);
    List<DailyLog> findByUser_IdAndLogDateBetween(Long userId, LocalDate start, LocalDate end);
}
