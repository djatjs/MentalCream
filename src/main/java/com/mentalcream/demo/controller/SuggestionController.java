package com.mentalcream.demo.controller;

import com.mentalcream.demo.dto.SuggestionDto;
import com.mentalcream.demo.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/suggestion")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @PostMapping("/generate")
    public ResponseEntity<SuggestionDto> generateSuggestion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate forDate) {
        SuggestionDto suggestionDto = suggestionService.generateSuggestion(forDate);
        return ResponseEntity.ok(suggestionDto);
    }
}
