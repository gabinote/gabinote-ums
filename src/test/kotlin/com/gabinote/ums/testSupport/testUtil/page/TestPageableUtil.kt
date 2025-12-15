package com.gabinote.ums.testSupport.testUtil.page


import com.gabinote.ums.common.dto.page.controller.PagedResControllerDto
import com.gabinote.ums.common.dto.sort.controller.SortResControllerDto
import io.mockk.mockk
import org.springframework.data.domain.*

object TestPageableUtil {
    fun Pageable.sortQueryParam(): String {
        return if (this.sort.isSorted) {
            this.sort.map { "${it.property},${it.direction}" }.joinToString(",")
        } else {
            ""
        }
    }

    fun createPageable(
        page: Int = 0,
        size: Int = 20,
        sortKey: String? = "id",
        sortDirection: Sort.Direction? = Sort.Direction.DESC
    ): Pageable {
        return sortKey?.let {
            val sort = Sort.by(sortDirection ?: Sort.Direction.ASC, it)
            PageRequest.of(page, size, sort)
        } ?: PageRequest.of(page, size)
    }

    fun <T> toPageObject(pageable: Pageable, dto: List<T>): Page<T> {
        return PageImpl<T>(
            dto,
            pageable,
            dto.size.toLong()
        )
    }

    fun <T> List<T>.toPage(pageable: Pageable): Page<T> {
        return PageImpl(
            this,
            pageable,
            this.size.toLong()
        )
    }

    fun <T> List<T>.toPagedResponse(pageable: Pageable): PagedResControllerDto<T> {
        val page = this.toPage(pageable)

        return page.toPagedResponse()
    }

    fun <T> Page<T>.toPagedResponse(): PagedResControllerDto<T> {
        return PagedResControllerDto(
            content = this.content,
            page = this.number,
            size = this.size,
            totalElements = this.totalElements,
            totalPages = this.totalPages,
            sortKey = this.sort.map {
                SortResControllerDto(
                    key = it.property,
                    direction = it.direction.name.lowercase()
                )
            }.toList()
        )
    }

    fun <T> List<T>.toMockPageObj(): Page<T> {
        return mockk<Page<T>>()
    }
}