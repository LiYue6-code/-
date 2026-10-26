package com.employment.common;

import java.util.List;

/**
 * 通用分页结果。
 */
public class PageResult<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final long total;

    public PageResult(List<T> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }

    public int getTotalPages() {
        if (size <= 0) {
            return 1;
        }
        return Math.max(1, (int) ((total + size - 1) / size));
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    /** 页码列表（用于渲染分页条，最多显示 5 个）。 */
    public List<Integer> getPageNumbers() {
        int totalPages = getTotalPages();
        int start = Math.max(1, page - 2);
        int end = Math.min(totalPages, start + 4);
        start = Math.max(1, end - 4);
        return java.util.stream.IntStream.rangeClosed(start, end).boxed().toList();
    }
}
