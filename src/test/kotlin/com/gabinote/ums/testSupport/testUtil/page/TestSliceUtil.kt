package com.gabinote.ums.testSupport.testUtil.page


import com.gabinote.ums.common.dto.slice.controller.SlicedResControllerDto
import com.gabinote.ums.common.dto.sort.controller.SortResControllerDto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

object TestSliceUtil {

    fun <T> List<T>.toSlice(pageable: Pageable): Slice<T> {
        val content = this
        return SliceImpl(
            content,
            pageable,
            true
        )
    }

    fun <T> Slice<T>.toSliceResponse(pageable: Pageable): SlicedResControllerDto<T> {
        return SlicedResControllerDto(
            content = this.content,
            page = pageable.pageNumber,
            size = pageable.pageSize,
            isLast = this.size < pageable.pageSize,
            sortKey = this.sort.map {
                SortResControllerDto(
                    key = it.property,
                    direction = it.direction.name.lowercase()
                )
            }.toList()
        )
    }

    fun <T> List<T>.toSliceResponse(pageable: Pageable): SlicedResControllerDto<T> {
        return this.toSlice(pageable).toSliceResponse(pageable)
    }
}