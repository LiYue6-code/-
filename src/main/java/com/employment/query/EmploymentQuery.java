package com.employment.query;

import java.time.LocalDate;

/**
 * 就业上报列表查询条件。
 */
public class EmploymentQuery {

    /** 关键字：学生姓名 / 学号 / 就业单位 / 岗位 */
    private String keyword;
    /** 状态：待审核 / 已确认 / 已退回；空表示全部 */
    private String status;
    private LocalDate from;
    private LocalDate to;

    private int page = 1;
    private int size = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getOffset() {
        return (Math.max(page, 1) - 1) * size;
    }
}
