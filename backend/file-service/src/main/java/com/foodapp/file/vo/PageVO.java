package com.foodapp.file.vo;

import java.util.List;

/**
 * 统一分页视图对象，结构遵循接口契约：{"total":100,"page":1,"size":10,"list":[...]}。
 *
 * @param <T> 列表项类型
 */
public class PageVO<T> {

    /** 总记录数 */
    private long total;
    /** 当前页码（从1开始） */
    private int page;
    /** 每页条数 */
    private int size;
    /** 当前页数据 */
    private List<T> list;

    public PageVO() {
    }

    public PageVO(long total, int page, int size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list;
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
