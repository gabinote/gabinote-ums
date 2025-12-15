package com.gabinote.ums.common.util.validation.collection

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * 컬렉션 내 각 요소의 길이를 검증하는 검증기
 * CollectionElementLength 애노테이션에 대한 검증 로직 구현
 * @author 황준서
 */
class CollectionElementLengthValidator : ConstraintValidator<CollectionElementLength, Collection<String>> {
    /**
     * 각 요소의 최대 길이
     */
    private var length: Int = 0

    /**
     * null 값 허용 여부
     */
    private var nullable: Boolean = false

    /**
     * 빈 문자열 허용 여부
     */
    private var notEmpty: Boolean = false

    /**
     * 검증 대상 리소스 이름
     */
    private var resourceName: String = "element"

    /**
     * 검증기 초기화
     * @param constraintAnnotation 검증 애노테이션
     */
    override fun initialize(constraintAnnotation: CollectionElementLength) {
        length = constraintAnnotation.length
        nullable = constraintAnnotation.nullable
        notEmpty = constraintAnnotation.notEmpty
        resourceName = constraintAnnotation.resourceName
    }

    /**
     * 컬렉션 내 모든 문자열 요소 유효성 검사
     * @param value 검증할 문자열 컬렉션
     * @param context 검증 컨텍스트
     * @return 유효성 여부
     */
    override fun isValid(
        value: Collection<String>,
        context: ConstraintValidatorContext
    ): Boolean {
        value.forEach {
            if (!checkString(it, context)) {
                return false
            }
        }
        return true
    }

    /**
     * 개별 문자열 요소 유효성 검사
     * @param value 검증할 문자열
     * @param context 검증 컨텍스트
     * @return 유효성 여부
     */
    private fun checkString(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            if (!nullable) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(
                    "$resourceName is required and cannot be null."
                ).addConstraintViolation()
                return false
            }
            return true
        }

        if (value.isEmpty()) {
            if (!notEmpty) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(
                    "$resourceName is required and cannot be empty."
                ).addConstraintViolation()
                return false
            }
            return true
        }

        if (value.length > length) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(
                "$resourceName length cannot exceed $length characters."
            ).addConstraintViolation()
            return false
        }
        return true
    }
}