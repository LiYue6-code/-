package com.employment.controller;

import com.employment.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 查询统计控制器（负责人端）。
 */
@Controller
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/admin/statistics")
    public String statistics(Model model) {
        model.addAttribute("stats", statisticsService.stats());
        return "admin/statistics";
    }
}
