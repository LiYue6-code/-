package com.employment.query;

/**
 * 负责人端招聘单位列表检索条件。
 */
public class AdminCompanyQuery {

    /** 关键字：单位名称 / 联系人 / 联系电话 */
    private String keyword;

    private int page = 1;
    private int size = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
