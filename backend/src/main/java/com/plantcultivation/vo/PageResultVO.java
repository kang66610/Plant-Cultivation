package com.plantcultivation.vo;

import lombok.Data;
import java.util.List;

@Data
public class PageResultVO<T> {
    private List<T> records;
    private long total;
    private long page;
    private long size;
    private long pages;

    public static <T> PageResultVO<T> of(List<T> records, long total, long page, long size) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        return result;
    }
}
