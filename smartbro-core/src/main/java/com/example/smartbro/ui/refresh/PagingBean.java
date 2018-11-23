package com.example.smartbro.ui.refresh;

/**
 * Created by Justin Wang from SmartBro on 12/12/17.
 * 分页的处理类
 */

public final class PagingBean {

    private int pageIndex = 0;
    private int total = 0;
    private int pageSize = 0;

    private int currentCount = 0;
    private int delayed = 0;

    public int getPageIndex() {
        return pageIndex;
    }

    public PagingBean setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }

    public int getTotal() {
        return total;
    }

    public PagingBean setTotal(int total) {
        this.total = total;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public PagingBean setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public PagingBean setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
        return this;
    }

    public int getDelayed() {
        return delayed;
    }

    public PagingBean setDelayed(int delayed) {
        this.delayed = delayed;
        return this;
    }
}
