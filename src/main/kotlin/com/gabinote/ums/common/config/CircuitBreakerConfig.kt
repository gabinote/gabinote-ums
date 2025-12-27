package com.gabinote.ums.common.config

import com.gabinote.ums.mail.service.MailService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.core.registry.EntryAddedEvent
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

@Configuration
class CircuitBreakerConfig(
    private val registry: CircuitBreakerRegistry,
    private val mailService: MailService
) {
    @PostConstruct
    fun registerEventListener() {
        // 정적 서킷
        registry.allCircuitBreakers.forEach { circuitBreaker ->
            attachLogListener(circuitBreaker)
        }

        // 동적 서킷 브레이커
        registry.eventPublisher.onEntryAdded { event: EntryAddedEvent<CircuitBreaker> ->
            attachLogListener(event.addedEntry)
        }
    }

    private fun attachLogListener(circuitBreaker: CircuitBreaker) {
        circuitBreaker.eventPublisher.onStateTransition { event ->
            val from = event.stateTransition.fromState
            val to = event.stateTransition.toState

            logger.warn { "CircuitBreaker '${circuitBreaker.name}' state changed from $from to $to" }

            if (to == CircuitBreaker.State.OPEN) {
                mailService.sendAdminAlert(
                    title = "[CRITICAL] (${circuitBreaker.name}) OPEN.",
                    message = "(${circuitBreaker.name}) 서킷 브레이커가 열렸습니다. 시스템 로그를 확인해주세요."
                )
            }

            if (from == CircuitBreaker.State.HALF_OPEN && to == CircuitBreaker.State.CLOSED) {
                mailService.sendAdminAlert(
                    title = "[CRITICAL] (${circuitBreaker.name}) CLOSED.",
                    message = "(${circuitBreaker.name}) 서킷 브레이커가 닫혔습니다. 시스템 로그를 확인해주세요."
                )
            }
        }
    }
}