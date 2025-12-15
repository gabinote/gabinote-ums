package com.gabinote.ums.common.util.validation.collection

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * 컬렉션 내 각 요소의 길이를 검증하는 애노테이션
 * 문자열 컬렉션에서 각 요소가 지정된 길이를 초과하지 않는지 확인
 * @author 황준서
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CollectionElementLengthValidator::class])
annotation class CollectionElementLength(
    /**
     * 각 요소의 최대 길이
     */
    val length: Int,

    /**
     * null 값 허용 여부
     */
    val nullable: Boolean = false,

    /**
     * 빈 컬렉션 허용 여부 (false이면 빈 컬렉션 허용)
     */
    val notEmpty: Boolean = false,

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
    val message: String = "Each element's length must not exceed {length} characters.",

    /**
     * 검증 페이로드
     */
    val payload: Array<KClass<out Payload>> = []
)
