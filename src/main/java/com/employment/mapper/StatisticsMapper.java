package com.employment.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * 统计相关数据访问接口。
 */
public interface StatisticsMapper {

    @Select("SELECT COUNT(*) FROM student")
    long countStudent();

    @Select("SELECT COUNT(*) FROM company")
    long countCompany();

    @Select("SELECT COUNT(*) FROM job")
    long countJob();

    @Select("SELECT COUNT(*) FROM job_application")
    long countApplication();

    @Select("SELECT COUNT(*) FROM employment_info")
    long countEmployment();

    @Select("SELECT COUNT(*) FROM job_application WHERE hire_status = '已录用'")
    long countHired();
}
