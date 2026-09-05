package com.wms.common.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结构。
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> r = new PageResult<>();
        r.records = page.getRecords();
        r.total = page.getTotal();
        r.pageNum = page.getCurrent();
        r.pageSize = page.getSize();
        return r;
    }
}
