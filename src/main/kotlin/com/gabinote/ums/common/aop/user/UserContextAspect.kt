package com.gabinote.ums.common.aop.user

import com.gabinote.ums.common.util.context.UserContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

private val logger = KotlinLogging.logger {}

@Order(1)
@Aspect
@Component
class UserContextAspect(
    private val userContext: UserContext
) {

    @Before("@within(org.springframework.web.bind.annotation.RestController)")
    fun initUserContext(joinPoint: JoinPoint) {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val uid = request.getHeader("X-Token-Sub")
        val roles = request.getHeader("X-Token-Roles")
        if(uid != null && roles != null) {
            userContext.setContext(
                uid = uid,
                roles = roles.split(",")
            )
        }

        logger.debug { "User context initialized with uid: ${userContext.uid} and roles: ${userContext.roles}" }
    }
}