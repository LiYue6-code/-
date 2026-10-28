package com.employment.query;

/**
 * 负责人端岗位列表检索条件。
 *
 * <p>与学生端 {@link JobQuery} 分开：学生端固定只查「招聘中」的岗位，
 * 负责人端需要按状态筛选，两者语义不同，共用一个对象会让参数互相干扰。
 */
public class AdminJobQuery {

    /** 关键字：岗位名称 / 招聘单位名称 */
    private String keyword;
    /** 招聘单位 ID；为空表示不限 */
    private Long companyId;
    /** 岗位状态：招聘中 / 已截止；为空表示全部 */
    private String status;

    private int page = 1;
    private int size = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    /** MyBatis LIMIT 偏移量。 */
    public int getOffset() {
        return (Math.max(page, 1) - 1) * size;
    }
}
