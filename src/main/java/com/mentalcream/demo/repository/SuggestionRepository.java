package com.mentalcream.demo.repository;

import com.mentalcream.demo.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    java.util.Optional<Suggestion> findByUser_IdAndLogDate(Long userId, java.time.LocalDate logDate);
    List<Suggestion> findTop3ByUser_IdAndLogDateBeforeOrderByLogDateDesc(Long userId, java.time.LocalDate date);
}
