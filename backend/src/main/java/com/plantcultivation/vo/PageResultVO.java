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
    private Boolean hasMore;
    private Long nextCursorId;
    private String nextCursorCreatedAt;

    public static <T> PageResultVO<T> of(List<T> records, long total, long page, long size) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        result.setHasMore(page < result.getPages());
        return result;
    }

    public static <T> PageResultVO<T> ofCursor(List<T> records, long size,
                                                boolean hasMore,
                                                Long nextCursorId,
                                                String nextCursorCreatedAt) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(records);
        result.setTotal(records.size());
        result.setPage(1);
        result.setSize(size);
        result.setPages(hasMore ? 2 : 1);
        result.setHasMore(hasMore);
        result.setNextCursorId(nextCursorId);
        result.setNextCursorCreatedAt(nextCursorCreatedAt);
        return result;
    }
}
