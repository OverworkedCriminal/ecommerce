package ecommerce.dto.shared;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;

@Builder
public record OutPage<T>(
    List<T> content,

    int pageIdx,
    int pageSize,
    int totalPages,
    long totalElements
) {
    public static <T> OutPage<T> from(Page<T> page) {
        final var pageable = page.getPageable();

        return OutPage.<T>builder()
            .content(page.getContent())
            .pageIdx(pageable.getPageNumber())
            .pageSize(pageable.getPageSize())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .build();
    }
}
