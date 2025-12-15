package com.gabinote.ums.common.util.validation.page.sort

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.data.domain.Pageable

/**
 * 페이지 정렬 키 유효성 검증기
 * PageSortKeyCheck 애노테이션에 대한 검증 로직을 구현
 * @author 황준서
 */
class PageSortKeyValidator : ConstraintValidator<PageSortKeyCheck, Pageable> {

    /**
     * 유효한 정렬 키 목록
     */
    private var sortKeys: List<String> = emptyList()

    /**
     * 검증기 초기화
     * @param constraintAnnotation 검증 애노테이션
     */
    override fun initialize(constraintAnnotation: PageSortKeyCheck) {
        sortKeys = constraintAnnotation.sortKey.java.enumConstants.map { it.key }
    }

    /**
     * 페이지 요청의 정렬 키 유효성 검사
     * @param value 검증할 Pageable 객체
     * @param context 검증 컨텍스트
     * @return 유효성 여부
     */
    override fun isValid(value: Pageable, context: ConstraintValidatorContext): Boolean {
        var result = true
        val notValidKeys = mutableListOf<String>()
        val sort = value.sort
        if (sort.isEmpty) {
            return true
        }

        for (order in sort) {
            if (!sortKeys.contains(order.property)) {
                notValidKeys.add(order.property)
                result = false
            }
        }

        if (!result) {
            val message = StringBuilder("Invalid sort key(s): ")
            message.append(notValidKeys.joinToString(","))
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(message.toString()).addConstraintViolation()
        }
        return result
    }

}