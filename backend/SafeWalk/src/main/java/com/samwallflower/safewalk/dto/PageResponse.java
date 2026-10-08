package com.samwallflower.safewalk.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public record PageResponse<T>(List<T> content,int page, int size, long totalElements, int totalPages, boolean last) {

    @Value("${DEFAULT_PAGE_SIZE}")
    private static int DEFAULT_MAX_PAGE_SIZE;

    public static <T> PageResponse<T> from(Page<T> page){
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public static Pageable pageRequest(int page, int size, Sort sort){
        return PageRequest.of(Math.max(page,0),
                Math.min(Math.max(size,1), DEFAULT_MAX_PAGE_SIZE), sort);
    }
}
