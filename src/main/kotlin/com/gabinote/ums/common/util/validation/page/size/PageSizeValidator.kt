package com.gabinote.ums.common.util.validation.page.size

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.data.domain.Pageable

/**
 * 페이지 크기 유효성 검증기
 * PageSizeCheck 애노테이션에 대한 검증 로직을 구현
 * @author 황준서
 */
class PageSizeValidator : ConstraintValidator<PageSizeCheck, Pageable> {

    /**
     * 최대 페이지 크기
     */
    private var max: Int = 0

    /**
     * 최소 페이지 크기
     */
    private var min: Int = 0

    /**
     * 검증기 초기화
     * @param constraintAnnotation 검증 애노테이션
     */
    override fun initialize(constraintAnnotation: PageSizeCheck) {
        max = constraintAnnotation.max
        min = constraintAnnotation.min
    }

    /**
     * 페이지 크기 유효성 검사
     * @param value 검증할 Pageable 객체
     * @param context 검증 컨텍스트
     * @return 유효성 여부
     */
    override fun isValid(value: Pageable, context: ConstraintValidatorContext): Boolean {
        if (value.pageSize in min..max) {
            return true
        }
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate("Page size must be between $min and $max. but was ${value.pageSize}")
            .addConstraintViolation()

        return false
    }

}