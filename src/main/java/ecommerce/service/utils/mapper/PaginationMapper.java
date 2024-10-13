package ecommerce.service.utils.mapper;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ecommerce.dto.shared.InPagination;
import ecommerce.dto.shared.OutPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaginationMapper {

    public static PageRequest intoPageRequest(InPagination pagination) {
        return PageRequest.of(
            pagination.pageIdx(),
            pagination.pageSize()
        );
    }

    public static <T, E> OutPage<T> fromPage(
        Page<E> pageEntities,
        Function<E, T> mapFn
    ) {
        final Page<T> pageDto = pageEntities.map(mapFn);
        final Pageable pageable = pageDto.getPageable();

        return OutPage.<T>builder()
            .content(pageDto.getContent())
            .pageIdx(pageable.getPageNumber())
            .pageSize(pageable.getPageSize())
            .totalPages(pageDto.getTotalPages())
            .totalElements(pageDto.getTotalElements())
            .build();
    }
}
