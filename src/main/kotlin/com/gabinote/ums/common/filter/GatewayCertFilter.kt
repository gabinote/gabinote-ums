package com.gabinote.ums.common.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class GatewayCertFilter(
    @Value("\${gabinote.common.gateway.secret}")
    private val gatewaySecret: String,
) : OncePerRequestFilter() {

    private val gatewaySecretHeader = "X-Gateway-Secret"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {

        val requestSecret = request.getHeader(gatewaySecretHeader)
        if (requestSecret != gatewaySecret) {
            logger.warn { "Gateway certification failed. Invalid secret: $requestSecret" }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
            return
        }
        filterChain.doFilter(request, response)

    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath

        return !path.startsWith("/api/")
    }
}