package com.mentalcream.demo.repository;

import com.mentalcream.demo.domain.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, LocalDate> {
    Optional<DailyLog> findByLogDate(LocalDate logDate);
    List<DailyLog> findByLogDateBetween(LocalDate start, LocalDate end);
}
