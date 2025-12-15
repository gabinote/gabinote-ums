package com.gabinote.ums.common.mapping.slice

import com.gabinote.ums.common.dto.slice.controller.SlicedResControllerDto
import com.gabinote.ums.common.dto.sort.controller.SortResControllerDto
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component

@Component
class SliceMapper {

    // Page<User>를 PageResponse<UserDto>로 변환하는 메서드
    fun <T> toSlicedResponse(slice: Slice<T>): SlicedResControllerDto<T> {
        return SlicedResControllerDto(
            content = slice.content,
            page = slice.number,
            size = slice.size,
            isLast = slice.isLast,
            sortKey = slice.sort.map {
                SortResControllerDto(
                    key = it.property,
                    direction = it.direction.name.lowercase()
                )
            }.toList()
        )
    }
}