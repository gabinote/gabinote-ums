package com.gabinote.ums.user.service.userWithdraw

import com.gabinote.ums.common.util.time.TimeProvider
import com.gabinote.ums.outbox.service.OutBoxService
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import com.gabinote.ums.user.dto.userWithdraw.service.PurgeKeycloakUserResServiceDto
import com.gabinote.ums.user.event.userPurge.ForcePurgeEvent
import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEventHelper
import com.gabinote.ums.user.event.userWithdraw.WithdrawProcess
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserService
import com.gabinote.ums.user.service.user.UserService
import com.gabinote.ums.user.service.withdrawProcessHistory.WithdrawProcessHistoryService
import com.gabinote.ums.user.service.withdrawRequest.WithdrawRequestService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.core.EventPublisher
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class UserWithdrawService(
    private val keycloakUserService: KeycloakUserService,
    private val withdrawRequestService: WithdrawRequestService,
    private val withdrawProcessHistoryService: WithdrawProcessHistoryService,
    private val outBoxService: OutBoxService,
    private val userService: UserService,
    private val timeProvider: TimeProvider,
    private val userWithdrawPurgeService: UserWithdrawPurgeService,
    private val publisher: ApplicationEventPublisher,
) {

    @Value("\${gabinote.withdraw.purge.batch-size}")
    lateinit var batchSize: String



    @Transactional
    fun withdrawUser(uid: UUID) {
        userService.delete(uid)
        withdrawRequestService.create(uid)
        withdrawProcessHistoryService.create(uid, WithdrawProcess.APPLICATION_USER_DELETE)
        outBoxService.createWithdrawEvent(uid)
        // request 생성시에는 keycloak 유저 비활성화만 함.
        // 삭제는 후에 스케줄러에서 일괄 처리
        // 이러면 별도의 로직 없이 동일 계정으로 탈퇴 및 재가입 시도 막는게 가능
        keycloakUserService.disableUser(uid.toString())
        logger.info { "User $uid has been withdrawn successfully." }
    }

    fun runForcePurgeWithdrawal() {
        publisher.publishEvent(ForcePurgeEvent())
    }


    // 기존 withdrawRequest 중 keycloak 유저 데이터가 남아 있는 유저의 데이터를 영구 삭제 처리
    fun purgeKeycloakUsers(targetStatus: WithdrawPurgeStatus) : PurgeKeycloakUserResServiceDto{
        // 스케줄러 동작 시간과 관계 없이 당일 0시 기준으로 처리
        val cutoffBaseDateTime = timeProvider.now().truncatedTo(ChronoUnit.DAYS)
        val totalItem = withdrawRequestService.getCntPendingRequests(cutoffBaseDateTime, targetStatus)
        val res = PurgeKeycloakUserResServiceDto(total=totalItem)
        processPurgeKeycloakUsers(
            totalItem = totalItem,
            res = res,
            cutoffBaseDateTime = cutoffBaseDateTime,
            targetStatus = targetStatus
        )
        logger.info { "Starting purge keycloakUsers targetStatus=${targetStatus.value} totalItem=$totalItem cutOffBate=$cutoffBaseDateTime" }
        return res
    }


    fun processPurgeKeycloakUsers(
        totalItem: Long,
        res: PurgeKeycloakUserResServiceDto,
        cutoffBaseDateTime: LocalDateTime,
        targetStatus: WithdrawPurgeStatus
    ) {
        val batchSize = batchSize.toInt()
        val batchPageable = PageRequest.of(0, batchSize)
        val totalPages = calTotalPage(totalItem, batchSize)
        (0 until totalPages).forEach { page ->
            val pendingRequests = withdrawRequestService.getAllPendingRequests(
                batchPageable,
                cutoffBaseDateTime,
                targetStatus
            )
            if (pendingRequests.isEmpty()) return
            logger.info { "Processing page=${page + 1}/$totalPages (Success: ${res.success}, Fail: ${res.failed})" }
            processBatchItem(pendingRequests, res)
        }

    }

    private fun processBatchItem(
        requests: List<WithdrawRequest>,
        res: PurgeKeycloakUserResServiceDto,
    ) {
        requests.forEach { request ->
            runCatching {
                userWithdrawPurgeService.purgeUser(request)
            }.onSuccess {
                res.success += 1
                logger.info { "User ${request.uid} has been deleted successfully." }
            }.onFailure { e ->
                logger.error(e) { "Failed to purge keycloak user with uid=${request.uid}" }
                res.failed += 1
                processPurgeFail(request)
            }

        }
    }

    private fun processPurgeFail(request: WithdrawRequest) {
        runCatching {
            userWithdrawPurgeService.processPurgeFail(request)
        }.onFailure { e ->
            logger.error(e) { "Failed to process purge fail for user with uid=${request.uid}" }
        }
    }

    private fun calTotalPage(totalItem: Long, batchSize: Int): Long =
        (totalItem / batchSize) + if (totalItem % batchSize > 0) 1 else 0


}