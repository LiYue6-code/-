package com.employment.entity;

import java.time.LocalDateTime;

/**
 * 岗位实体。companyName 为关联查询出的冗余字段，用于列表展示。
 */
public class Job {
    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private String requirement;
    private Integer headcount;
    private String salary;
    private String status;
    private LocalDateTime publishTime;

    /** 冗余字段：招聘单位名称 */
    private String companyName;

    /** 展示字段：当前学生是否已申请该岗位（不落库） */
    private boolean applied;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public Integer getHeadcount() {
        return headcount;
    }

    public void setHeadcount(Integer headcount) {
        this.headcount = headcount;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    /** 是否仍在招聘，用于判断学生能否申请。 */
    public boolean isOpen() {
        return "招聘中".equals(status);
    }
}
