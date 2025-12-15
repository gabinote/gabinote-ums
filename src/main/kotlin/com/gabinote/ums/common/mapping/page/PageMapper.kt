package com.gabinote.ums.common.mapping.page

import com.gabinote.ums.common.dto.page.controller.PagedResControllerDto
import com.gabinote.ums.common.dto.sort.controller.SortResControllerDto
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class PageMapper {

    // Page<User>를 PageResponse<UserDto>로 변환하는 메서드
    fun <T> toPagedResponse(page: Page<T>): PagedResControllerDto<T> {
        return PagedResControllerDto(
            content = page.content,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,

            sortKey = page.sort.map {
                SortResControllerDto(
                    key = it.property,
                    direction = it.direction.name.lowercase()
                )
            }.toList()
        )
    }
}