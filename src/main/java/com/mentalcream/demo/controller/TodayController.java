package com.mentalcream.demo.controller;

import com.mentalcream.demo.dto.DailyLogDto;
import com.mentalcream.demo.dto.DoneItemDto;
import com.mentalcream.demo.dto.request.AddDoneItemRequest;
import com.mentalcream.demo.dto.request.UpdateDailyLogRequest;
import com.mentalcream.demo.dto.response.TodayResponse;
import com.mentalcream.demo.service.TodayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Today Log API", description = "오늘의 활동 및 로그 관리를 위한 API")
@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
public class TodayController {

    private final TodayService todayService;

    @Operation(summary = "오늘의 화면 데이터 조회", description = "지정된 날짜의 전체 활동 요약 및 로그를 반환합니다.")
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
