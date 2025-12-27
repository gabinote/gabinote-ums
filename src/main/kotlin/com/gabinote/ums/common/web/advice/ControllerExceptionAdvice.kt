package com.gabinote.ums.common.web.advice

import com.gabinote.ums.common.util.exception.controller.GatewayAuthFailed
import com.gabinote.ums.common.util.exception.service.ResourceNotFound
import com.gabinote.ums.common.util.log.ErrorLog
import com.gabinote.ums.common.web.advice.ExceptionAdviceHelper.getRequestId
import com.gabinote.ums.common.web.advice.ExceptionAdviceHelper.problemDetail
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

/**
 * 컨트롤러 계층 예외 처리를 위한 어드바이스 클래스
 * 컨트롤러에서 발생하는 예외를 처리하는 최우선 어드바이스
 * @author 황준서
 */
@Order(1)
@RestControllerAdvice
class ControllerExceptionAdvice{
    @ExceptionHandler(GatewayAuthFailed::class)
    fun handleGatewayAuthFailed(
        ex:GatewayAuthFailed,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val problemDetail = problemDetail(
            status = status,
            title = "Gateway Authentication Failed",
            detail = ex.errorMessage,
            requestId = requestId
        )
        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = status,
            error = "GatewayAuthFailed",
            message = ex.errorMessage
        )
        logger.error { log.toString() }
        return ResponseEntity(problemDetail, status)
    }
}