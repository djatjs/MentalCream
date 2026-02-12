package com.mentalcream.demo.repository;

import com.mentalcream.demo.domain.DoneItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoneItemRepository extends JpaRepository<DoneItem, Long> {
    List<DoneItem> findByDailyLog_LogDateIn(List<LocalDate> dates);
    List<DoneItem> findByDailyLog_LogDateBetween(LocalDate start, LocalDate end);
}
