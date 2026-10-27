package com.employment.query;

/**
 * 学生端岗位检索条件。
 *
 * <p>学生端只检索「招聘中」的岗位，因此状态不作为筛选条件，
 * 由 Mapper 中的 SQL 固定过滤。
 */
public class JobQuery {

    /** 关键字：岗位名称 / 招聘单位名称 */
    private String keyword;
    /** 招聘单位 ID；为空表示不限 */
    private Long companyId;

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
