package com.gabinote.ums.common.util.validation.page.size

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * 페이지 크기 유효성 검사를 위한 애노테이션
 * 페이지 크기가 지정된 최소/최대 범위 내에 있는지 확인
 * @author 황준서
 */
@Constraint(validatedBy = [PageSizeValidator::class])
@Target(*[AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER])
@Retention(AnnotationRetention.RUNTIME)
annotation class PageSizeCheck(
    /**
     * 검증 그룹
     */
    val groups: Array<KClass<*>> = [],

    /**
     * 검증 페이로드
     */
    val payload: Array<KClass<out Payload>> = [],

    /**
     * 최대 페이지 크기
     */
    val max: Int = 0,

    /**
     * 최소 페이지 크기
     */
    val min: Int = 0,

    /**
     * 검증 실패 시 메시지
     */
    val message: String = "Page size not valid"
)
