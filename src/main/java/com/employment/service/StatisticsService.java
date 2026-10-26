package com.employment.service;

import com.employment.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统计业务。
 */
@Service
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public StatisticsService(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    /** 汇总各类统计数据。 */
    public Map<String, Long> stats() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("学生数", statisticsMapper.countStudent());
        result.put("招聘单位数", statisticsMapper.countCompany());
        result.put("岗位数", statisticsMapper.countJob());
        result.put("申请数", statisticsMapper.countApplication());
        result.put("就业上报数", statisticsMapper.countEmployment());
        result.put("已录用数", statisticsMapper.countHired());
        return result;
    }
}
