package com.gabinote.ums.user.service.userWithdraw

import com.gabinote.ums.common.util.time.TimeProvider
import com.gabinote.ums.mail.service.MailService
import com.gabinote.ums.outbox.service.OutBoxService
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import com.gabinote.ums.user.event.userWithdraw.WithdrawProcess
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserService
import com.gabinote.ums.user.service.user.UserService
import com.gabinote.ums.user.service.withdrawProcessHistory.WithdrawProcessHistoryService
import com.gabinote.ums.user.service.withdrawRequest.WithdrawRequestService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserWithdrawPurgeService(
    private val keycloakUserService: KeycloakUserService,
    private val withdrawRequestService: WithdrawRequestService,
    private val withdrawProcessHistoryService: WithdrawProcessHistoryService,
    private val mailService: MailService,
) {

    @Value("\${gabinote.withdraw.purge.max-retry-attempts}")
    lateinit var maxRetryAttempts: String


    /**
     * 해당 WithdrawRequest 유저의 Keycloak 데이터 삭제, WithdrawRequest 상태를 COMPLETED로 변경 및 WithdrawProcessHistory 기록을 생성한다.
     * @param withdrawRequest 처리할 WithdrawRequest 객체
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun purgeUser(withdrawRequest: WithdrawRequest) {
        withdrawRequestService.updatePurgeStatus(
            old = withdrawRequest,
            newStatus = WithdrawPurgeStatus.COMPLETED,
            newRetryCnt = withdrawRequest.purgeTryCnt,
        )
        withdrawProcessHistoryService.create(UUID.fromString(withdrawRequest.uid), WithdrawProcess.KEYCLOAK_USER_DELETE)
        keycloakUserService.deleteUser(withdrawRequest.uid)

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun processPurgeFail(withdrawRequest: WithdrawRequest){
        if (isOverMaxRetryAttempts(withdrawRequest)) {
            updateStatusToFailed(withdrawRequest)
        } else {
            updateStatusToRetrying(withdrawRequest)
        }
    }


    private fun isOverMaxRetryAttempts(withdrawRequest: WithdrawRequest): Boolean {
        return withdrawRequest.purgeTryCnt + 1 >= maxRetryAttempts.toLong()
    }

    private fun updateStatusToRetrying(withdrawRequest: WithdrawRequest){
        withdrawRequestService.updatePurgeStatus(
            old = withdrawRequest,
            newStatus = WithdrawPurgeStatus.RETRYING,
            newRetryCnt = withdrawRequest.purgeTryCnt + 1,
        )
        withdrawProcessHistoryService.create(
            uid = UUID.fromString(withdrawRequest.uid),
            process = WithdrawProcess.KEYCLOAK_USER_DELETE,
            isPassed = false
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateStatusToFailed(withdrawRequest: WithdrawRequest){
        withdrawRequestService.updatePurgeStatus(
            old = withdrawRequest,
            newStatus = WithdrawPurgeStatus.FAILED,
            newRetryCnt = withdrawRequest.purgeTryCnt,
        )
        withdrawProcessHistoryService.create(
            uid = UUID.fromString(withdrawRequest.uid),
            process = WithdrawProcess.KEYCLOAK_USER_DELETE,
            isPassed = false
        )
        mailService.sendAdminAlert(
          title = "[Critical] User Purge Failed Alert",
          message = "User with UID ${withdrawRequest.uid} has exceeded the maximum retry attempts for user data purge."
        )

    }

}