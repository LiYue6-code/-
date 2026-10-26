package com.employment.controller;

import com.employment.common.CsvExporter;
import com.employment.entity.OverviewVo;
import com.employment.entity.StatItem;
import com.employment.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 查询统计控制器（负责人端）：多维度统计、图表数据、CSV 导出。
 */
@Controller
public class StatisticsController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/admin/statistics")
    public String statistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {

        model.addAttribute("overview", statisticsService.overview(start, end));
        model.addAttribute("jobRank", statisticsService.jobRank(start, end));
        model.addAttribute("companyRank", statisticsService.companyRank(start, end));
        model.addAttribute("majorDistribution", statisticsService.majorDistribution(start, end));
        model.addAttribute("monthlyTrend", statisticsService.monthlyTrend(start, end));
        model.addAttribute("applicationStatus", statisticsService.applicationStatus(start, end));
        model.addAttribute("destination", statisticsService.employmentDestination());
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "admin/statistics";
    }

    /** 导出统计报表（CSV，Excel 可直接打开）。 */
    @GetMapping("/admin/statistics/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        OverviewVo overview = statisticsService.overview(start, end);

        CsvExporter csv = new CsvExporter();
        csv.row("就业管理系统 统计报表");
        csv.row("统计区间", rangeText(start, end));
        csv.row("生成时间", LocalDateTime.now().format(DATE_TIME));
        csv.row();

        csv.row("【概览指标】").row("指标", "数值");
        csv.row("学生总数", overview.getStudents());
        csv.row("招聘单位数", overview.getCompanies());
        csv.row("岗位数", overview.getJobs());
        csv.row("申请总数", overview.getApplications());
        csv.row("已录用数", overview.getHired());
        csv.row("就业信息已确认数", overview.getEmploymentConfirmed());
        csv.row("就业信息待审核数", overview.getEmploymentPending());
        csv.row("就业率", overview.getEmploymentRateText());
        csv.row();

        appendSection(csv, "【岗位申请量 Top10】", "岗位", "招聘单位", statisticsService.jobRank(start, end));
        appendSection(csv, "【单位招聘量 Top10】", "招聘单位", "单位性质", statisticsService.companyRank(start, end));
        appendSection(csv, "【专业分布】", "专业", "", statisticsService.majorDistribution(start, end));
        appendSection(csv, "【申请月度趋势】", "月份", "", statisticsService.monthlyTrend(start, end));
        appendSection(csv, "【申请审核状态分布】", "状态", "", statisticsService.applicationStatus(start, end));
        appendSection(csv, "【就业去向分布】", "就业单位", "", statisticsService.employmentDestination());

        String filename = "就业统计报表_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, CsvExporter.attachment(filename))
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv.toBytes());
    }

    private static void appendSection(CsvExporter csv, String title, String keyHeader,
                                      String extraHeader, List<StatItem> items) {
        csv.row(title).row(keyHeader, extraHeader, "数量");
        if (items == null || items.isEmpty()) {
            csv.row("暂无数据");
        } else {
            for (StatItem item : items) {
                csv.row(item.getLabel(), item.getExtra() == null ? "" : item.getExtra(), item.getAmount());
            }
        }
        csv.row();
    }

    private static String rangeText(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "全部";
        }
        return (start == null ? "不限" : start.toString()) + " 至 " + (end == null ? "不限" : end.toString());
    }
}
