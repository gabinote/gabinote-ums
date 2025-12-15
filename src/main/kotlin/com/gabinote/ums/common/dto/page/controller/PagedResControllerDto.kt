package com.gabinote.ums.common.dto.page.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.gabinote.ums.common.dto.sort.controller.SortResControllerDto

/**
 * 페이지네이션된 응답을 위한 컨트롤러 계층 DTO
 * 페이지 정보와 콘텐츠를 포함
 * @param T 페이지 내 항목의 타입
 * @author 황준서
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PagedResControllerDto<T>(
    /**
     * 페이지 내 항목 목록
     */
    val content: MutableList<T>? = null,

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    val page: Int = 0,

    /**
     * 페이지 크기
     */
    val size: Int = 0,

    /**
     * 전체 항목 수
     */
    val totalElements: Long = 0,

    /**
     * 전체 페이지 수
     */
    val totalPages: Int = 0,

    /**
     * 정렬 키 정보
     */
    val sortKey: List<SortResControllerDto>?
)