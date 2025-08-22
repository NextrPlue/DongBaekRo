package com.redstonetorch.dongbaekro.common.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PagedResponse<T>(
	List<T> content,
	Integer page,
	Integer size,
	Long totalElements,
	Integer totalPages,
	Boolean first,
	Boolean last
) {
	public PagedResponse(Page<T> page) {
		this(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(),
			page.isFirst(), page.isLast());
	}

	public static <T> PagedResponse<T> of(Page<T> page) {
		return new PagedResponse<>(page);
	}
}
