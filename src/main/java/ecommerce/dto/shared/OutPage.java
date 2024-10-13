package ecommerce.dto.shared;

import java.util.List;

import lombok.Builder;

@Builder
public record OutPage<T>(
    List<T> content,

    Integer pageIdx,
    Integer pageSize,
    Integer totalPages,
    Long totalElements
) {}
