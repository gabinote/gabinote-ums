package com.gabinote.ums.user.event.userPurge

import com.gabinote.ums.user.scheduler.UserWithdrawScheduler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class UserPurgeEventListener(
    private val userWithdrawScheduler: UserWithdrawScheduler,
) {

    @EventListener
    @Async
    fun handleForcePurgeEvent(event: ForcePurgeEvent) {
        userWithdrawScheduler.runWithdrawalPurge()
    }

}