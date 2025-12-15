package com.gabinote.ums.common.dto.slice.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.gabinote.ums.common.dto.sort.controller.SortResControllerDto

/**
 * 슬라이스된 응답을 위한 컨트롤러 계층 DTO
 * 페이지네이션보다 가벼운 슬라이스 정보와 콘텐츠를 포함
 * @param T 슬라이스 내 항목의 타입
 * @author 황준서
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SlicedResControllerDto<T>(
    /**
     * 슬라이스 내 항목 목록
     */
    val content: List<T>,

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    val page: Int,

    /**
     * 슬라이스 크기
     */
    val size: Int,

    /**
     * 마지막 슬라이스 여부
     */
    val isLast: Boolean,

    /**
     * 정렬 키 정보
     */
    val sortKey: List<SortResControllerDto>?
)