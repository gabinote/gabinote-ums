package com.gabinote.ums.common.dto.sort.controller

/**
 * 정렬 정보를 나타내는 컨트롤러 계층 DTO
 * 정렬 키와 방향을 포함
 * @author 황준서
 */
data class SortResControllerDto(
    /**
     * 정렬 키
     */
    val key: String,

    /**
     * 정렬 방향 (ASC/DESC)
     */
    val direction: String
)