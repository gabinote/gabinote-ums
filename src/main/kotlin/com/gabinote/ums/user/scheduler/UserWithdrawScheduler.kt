package com.gabinote.ums.user.scheduler

import com.gabinote.ums.common.util.time.TimeProvider
import com.gabinote.ums.mail.service.MailService
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.service.userWithdraw.UserWithdrawService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bouncycastle.math.Primes
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Component
class UserWithdrawScheduler(
    private val userWithdrawService: UserWithdrawService,
    private val mailService: MailService,
    private val timeProvider: TimeProvider
) {


    // 매일 새벽 3시에 keycloak 비활성된 유저 영구 삭제 처리
    @Scheduled(cron = "\${gabinote.withdraw.purge.schedule}")
    fun runWithdrawalPurge() {
        val start = timeProvider.now()
        mailService.sendAdminAlert("[INFO] UMS Keycloak Withdraw 실행", "$start 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 실행되었습니다.")
        logger.info { "start running withdraw user scheduler at $start" }

        runCatching {
            val retryingRes = userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.RETRYING)
            logger.info { "purge keycloak users $retryingRes" }
            val pendingRes = userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.PENDING)
            logger.info { "purge keycloak users $pendingRes" }
            retryingRes to pendingRes
        }.onSuccess {(retryingRes, pendingRes) ->
            val end = timeProvider.now()
            logger.info { "finished running withdraw user scheduler at $end" }
            mailService.sendAdminAlert("[INFO] UMS Keycloak Withdraw 완료",
                "$end 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 정상적으로 완료되었습니다. " +
                        "소요 시간: ${ChronoUnit.MILLIS.between(start,end)} ms" +
                        "\n처리 결과: RETRYING Scope : ${retryingRes.success} / ${retryingRes.total} PENDING Scope : ${pendingRes.success} / ${pendingRes.total}"
            )
        }.onFailure { e ->
            logger.error(e) { "error occurred while running withdraw user scheduler at $start" }
            mailService.sendAdminAlert("[ERROR] UMS Keycloak Withdraw 에러 발생", "Keycloak 탈퇴 유저 영구 삭제 스케줄러 실행 중 에러가 발생했습니다.\n에러 내용: ${e.message}")
        }

    }

}