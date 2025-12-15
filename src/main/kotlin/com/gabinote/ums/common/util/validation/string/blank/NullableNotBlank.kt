package com.gabinote.ums.common.util.validation.string.blank

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * null 값은 허용하지만 빈 문자열은 허용하지 않는 유효성 검사 애노테이션
 * 문자열이 null이거나 값이 있어야 함 (비어있거나 공백만 있으면 안 됨)
 * @author 황준서
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NullableNotBlankValidator::class])
annotation class NullableNotBlank(
    /**
     * 검증 대상 리소스 이름 (오류 메시지에 사용)
     */
    val resourceName: String = "element",

    /**
     * 검증 그룹
     */
    val groups: Array<KClass<*>> = [],

    /**
     * 검증 실패 시 메시지
     */
    val message: String = "Each resourceName must not be blank",

    /**
     * 검증 페이로드
     */
    val payload: Array<KClass<out Payload>> = []
)
