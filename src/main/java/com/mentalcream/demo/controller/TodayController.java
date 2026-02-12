package com.mentalcream.demo.controller;

import com.mentalcream.demo.dto.DailyLogDto;
import com.mentalcream.demo.dto.DoneItemDto;
import com.mentalcream.demo.dto.request.AddDoneItemRequest;
import com.mentalcream.demo.dto.request.UpdateDailyLogRequest;
import com.mentalcream.demo.dto.response.TodayResponse;
import com.mentalcream.demo.service.TodayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
public class TodayController {

    private final TodayService todayService;

    @GetMapping
    public ResponseEntity<TodayResponse> getTodayScreen(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        TodayResponse todayResponse = todayService.getTodayScreen(targetDate);
        return ResponseEntity.ok(todayResponse);
    }

    @PutMapping
    public ResponseEntity<DailyLogDto> upsertDailyLog(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody UpdateDailyLogRequest request) {
        DailyLogDto dailyLogDto = todayService.upsertDailyLog(date, request);
        return ResponseEntity.ok(dailyLogDto);
    }

    @PostMapping("/done")
    public ResponseEntity<DoneItemDto> addDoneItem(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody AddDoneItemRequest request) {
        DoneItemDto doneItemDto = todayService.addDoneItem(date, request);
        return new ResponseEntity<>(doneItemDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/done/{id}")
    public ResponseEntity<Void> deleteDoneItem(@PathVariable Long id) {
        todayService.deleteDoneItem(id);
        return ResponseEntity.noContent().build();
    }
}
