package com.employment.mapper;

import com.employment.entity.StatItem;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计相关数据访问接口（SQL 见 resources/mapper/StatisticsMapper.xml）。
 */
public interface StatisticsMapper {

    long countStudent();

    long countCompany();

    long countJob();

    long countApplication(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countHired(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countEmploymentConfirmed();

    long countEmploymentPending();

    /** 按岗位统计申请数（Top N）。 */
    List<StatItem> applicationsByJob(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                     @Param("limit") int limit);

    /** 按招聘单位统计申请数（Top N）。 */
    List<StatItem> applicationsByCompany(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                         @Param("limit") int limit);

    /** 按专业统计申请数。 */
    List<StatItem> applicationsByMajor(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** 按月统计申请数趋势。 */
    List<StatItem> applicationsByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** 申请审核状态分布。 */
    List<StatItem> applicationStatusDistribution(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** 就业去向分布（已确认的就业单位）。 */
    List<StatItem> employmentDestination();
}
