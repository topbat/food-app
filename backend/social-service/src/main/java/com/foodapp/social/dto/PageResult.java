package com.foodapp.social.dto;

import java.util.List;

/**
 * 统一分页响应结构：{total,page,size,list}。
 *
 * @param <T> 列表项类型
 */
public class PageResult<T> {

    /** 总记录数 */
    private long total;
    /** 当前页码（从1开始） */
    private int page;
    /** 每页条数 */
    private int size;
    /** 当前页数据 */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(long total, int page, int size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list;
    }

    /**
     * 构造分页响应。
     *
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页条数
     * @param list  当前页数据
     * @return 分页响应
     */
    public static <T> PageResult<T> of(long total, int page, int size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
}
