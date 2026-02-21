package com.mentalcream.demo.controller;

import com.mentalcream.demo.dto.response.TodayResponse;
import com.mentalcream.demo.service.TodayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final TodayService todayService;

    @GetMapping("/swagger")
    public String swagger() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/today-view")
    public String today(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        TodayResponse todayScreenData = todayService.getTodayScreen(targetDate);
        model.addAttribute("today", todayScreenData);
        return "today";
    }

    @GetMapping("/stats")
    public String stats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart, Model model) {
        LocalDate start = (weekStart != null) ? weekStart : LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        model.addAttribute("weekStart", start);
        return "stats";
    }
}
