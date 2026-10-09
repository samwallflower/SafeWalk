package com.samwallflower.safewalk.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    // The maximum page size the API allows (same value as DEFAULT_PAGE_SIZE in application.properties).
    private static final int MAX_PAGE_SIZE = 50;

    @Test
    void pageRequest_usesTheGivenPageAndSize() {
        Pageable pageable = PageResponse.pageRequest(2, 10);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void pageRequest_keepsTheGivenSort() {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageResponse.pageRequest(0, 25, sort);

        assertThat(pageable.getSort()).isEqualTo(sort);
        assertThat(pageable.getPageSize()).isEqualTo(25);
    }

    @Test
    void pageRequest_turnsANegativePageIntoPageZero() {
        assertThat(PageResponse.pageRequest(-5, 10).getPageNumber()).isEqualTo(0);
    }

    @Test
    void pageRequest_turnsASizeBelowOneIntoOne() {
        assertThat(PageResponse.pageRequest(0, 0).getPageSize()).isEqualTo(1);
        assertThat(PageResponse.pageRequest(0, -3).getPageSize()).isEqualTo(1);
    }

    @Test
    void pageRequest_capsTheSizeAtTheMaximum() {
        assertThat(PageResponse.pageRequest(0, 100_000).getPageSize()).isEqualTo(MAX_PAGE_SIZE);
        assertThat(PageResponse.pageRequest(0, 100_000, Sort.unsorted()).getPageSize()).isEqualTo(MAX_PAGE_SIZE);
    }

    @Test
    void from_copiesThePageFieldsIncludingTheTotals() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.last()).isFalse();
    }
}
