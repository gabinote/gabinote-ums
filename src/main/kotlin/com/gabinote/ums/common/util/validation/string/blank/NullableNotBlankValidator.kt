package com.gabinote.ums.common.util.validation.string.blank

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/**
 * NullableNotBlank 애노테이션에 대한 유효성 검증기
 * null 값은 허용하지만 빈 문자열은 허용하지 않는 검증 로직 구현
 * @author 황준서
 */
class NullableNotBlankValidator : ConstraintValidator<NullableNotBlank, String?> {
    /**
     * 검증 대상 리소스 이름
     */
    private var resourceName: String = "element"

    /**
     * 검증기 초기화
     * @param constraintAnnotation 검증 애노테이션
     */
    override fun initialize(constraintAnnotation: NullableNotBlank) {
        resourceName = constraintAnnotation.resourceName
    }

    /**
     * 문자열 값의 유효성 검사
     * null이면 유효, 값이 있으면 공백이 아니어야 함
     * @param value 검증할 문자열 값
     * @param context 검증 컨텍스트
     * @return 유효성 여부
     */
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }
        if (value.isBlank()) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(
                "$resourceName must not be blank."
            ).addConstraintViolation()
            return false
        }
        return true
    }
}