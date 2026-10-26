package com.employment.entity;

/**
 * 统计概览指标。
 */
public class OverviewVo {
    private final long students;
    private final long companies;
    private final long jobs;
    private final long applications;
    private final long hired;
    private final long employmentConfirmed;
    private final long employmentPending;
    private final double employmentRate;

    public OverviewVo(long students, long companies, long jobs, long applications,
                      long hired, long employmentConfirmed, long employmentPending,
                      double employmentRate) {
        this.students = students;
        this.companies = companies;
        this.jobs = jobs;
        this.applications = applications;
        this.hired = hired;
        this.employmentConfirmed = employmentConfirmed;
        this.employmentPending = employmentPending;
        this.employmentRate = employmentRate;
    }

    public long getStudents() {
        return students;
    }

    public long getCompanies() {
        return companies;
    }

    public long getJobs() {
        return jobs;
    }

    public long getApplications() {
        return applications;
    }

    public long getHired() {
        return hired;
    }

    public long getEmploymentConfirmed() {
        return employmentConfirmed;
    }

    public long getEmploymentPending() {
        return employmentPending;
    }

    public double getEmploymentRate() {
        return employmentRate;
    }

    /** 就业率展示文本，如 23.5%。 */
    public String getEmploymentRateText() {
        return String.format("%.1f%%", employmentRate);
    }
}
