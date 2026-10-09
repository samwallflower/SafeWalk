package com.samwallflower.safewalk.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public record PageResponse<T>(List<T> content,int page, int size, long totalElements, int totalPages, boolean last) {

    private static final int MAX_PAGE_SIZE=50;

    public static <T> PageResponse<T> from(Page<T> page){
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public static Pageable pageRequest(int page, int size, Sort sort){
        return PageRequest.of(Math.max(page,0),
                Math.min(Math.max(size,1), MAX_PAGE_SIZE), sort);
    }

    public static Pageable pageRequest(int page, int size){
        return PageRequest.of(Math.max(page,0),
                Math.min(Math.max(size,1), MAX_PAGE_SIZE));
    }
}
