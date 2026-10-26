package com.employment.service;

import com.employment.entity.OverviewVo;
import com.employment.entity.StatItem;
import com.employment.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 查询统计业务：概览指标 + 多维度分布（岗位/单位/专业/月份/状态/就业去向）。
 */
@Service
public class StatisticsService {

    private static final int TOP_N = 10;

    private final StatisticsMapper statisticsMapper;

    public StatisticsService(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    /** 概览指标。 */
    public OverviewVo overview(LocalDate from, LocalDate to) {
        long students = statisticsMapper.countStudent();
        long confirmed = statisticsMapper.countEmploymentConfirmed();
        double rate = students == 0 ? 0D : Math.round(confirmed * 1000.0 / students) / 10.0;
        return new OverviewVo(
                students,
                statisticsMapper.countCompany(),
                statisticsMapper.countJob(),
                statisticsMapper.countApplication(from, to),
                statisticsMapper.countHired(from, to),
                confirmed,
                statisticsMapper.countEmploymentPending(),
                rate);
    }

    public List<StatItem> jobRank(LocalDate from, LocalDate to) {
        return fillPercent(statisticsMapper.applicationsByJob(from, to, TOP_N));
    }

    public List<StatItem> companyRank(LocalDate from, LocalDate to) {
        return fillPercent(statisticsMapper.applicationsByCompany(from, to, TOP_N));
    }

    public List<StatItem> majorDistribution(LocalDate from, LocalDate to) {
        return fillPercent(statisticsMapper.applicationsByMajor(from, to));
    }

    public List<StatItem> monthlyTrend(LocalDate from, LocalDate to) {
        return fillPercent(statisticsMapper.applicationsByMonth(from, to));
    }

    public List<StatItem> applicationStatus(LocalDate from, LocalDate to) {
        return fillPercent(statisticsMapper.applicationStatusDistribution(from, to));
    }

    public List<StatItem> employmentDestination() {
        return fillPercent(statisticsMapper.employmentDestination());
    }

    /** 按最大值换算百分比，供前端画条形图。 */
    private static List<StatItem> fillPercent(List<StatItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        long max = items.stream().mapToLong(StatItem::getAmount).max().orElse(0L);
        for (StatItem item : items) {
            item.setPercent(max == 0 ? 0D : Math.round(item.getAmount() * 1000.0 / max) / 10.0);
        }
        return items;
    }
}
