package com.gabinote.ums.common.util.validation.page.sort

import com.gabinote.ums.common.domain.base.BaseSortKey
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * 페이지 정렬 키 유효성 검사를 위한 애노테이션
 * 정렬 키가 지정된 BaseSortKey 구현체의 유효한 키인지 확인
 * @author 황준서
 */
@Constraint(validatedBy = [PageSortKeyValidator::class])
@Target(*[AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER])
@Retention(AnnotationRetention.RUNTIME)
annotation class PageSortKeyCheck(
    /**
     * 검증 그룹
     */
    val groups: Array<KClass<*>> = [],

    /**
     * 검증 페이로드
     */
    val payload: Array<KClass<out Payload>> = [],

    /**
     * 검증할 정렬 키 열거형 클래스
     */
    val sortKey: KClass<out BaseSortKey>,

    /**
     * 검증 실패 시 메시지
     */
    val message: String = "Page sort key not valid"
)
