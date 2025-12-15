package com.gabinote.ums.common.domain.base

/**
 * 정렬 키를 정의하는 기본 인터페이스
 * 모든 정렬 키 열거형에서 구현해야 함
 * @author 황준서
 */
interface BaseSortKey {
    /**
     * 정렬에 사용되는 속성 키
     */
    val key: String
}