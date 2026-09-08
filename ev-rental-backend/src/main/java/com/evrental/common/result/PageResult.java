package com.evrental.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> implements Serializable {

    private long current;
    private long size;
    private long total;
    private long pages;
    private List<T> records;

    public PageResult() {}

    public PageResult(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        this.pages = (total + size - 1) / size;
    }
}
