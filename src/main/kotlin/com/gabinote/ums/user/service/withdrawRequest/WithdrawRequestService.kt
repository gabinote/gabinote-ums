package com.gabinote.ums.user.service.withdrawRequest

import com.gabinote.ums.common.util.exception.service.ServerError
import com.gabinote.ums.policy.domain.policy.PolicyKey
import com.gabinote.ums.policy.service.policy.PolicyService
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequestRepository
import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEvent
import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEventHelper
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserService
import org.bouncycastle.math.Primes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class WithdrawRequestService(
    private val withdrawRequestRepository: WithdrawRequestRepository,
    private val keycloakUserService: KeycloakUserService,
    private val policyService: PolicyService
) {

    fun fetchByUid(uid: UUID): WithdrawRequest {
        return withdrawRequestRepository.findByUid(uid.toString())
            ?: throw ServerError("UserWithdraw with uid $uid not found")
    }

    @Transactional
    fun create(uid: UUID) : WithdrawRequest {
        val userEmail = keycloakUserService.getUserEmail(uid.toString())
        val withdrawRequest = WithdrawRequest(
            uid = uid.toString(),
            email = userEmail,
            purgeStatus = WithdrawPurgeStatus.PENDING.value
        )
        val saved = withdrawRequestRepository.save(withdrawRequest)
        return saved
    }

    private fun calCutoffCreatedDate(targetTime: LocalDateTime): LocalDateTime {
        val cutOff = policyService.getByKey(PolicyKey.USER_PURGE_CUTOFF_DAYS).toLong()
        return targetTime.minusDays(cutOff)
    }



    fun getAllPendingRequests(pageable: Pageable,targetTime: LocalDateTime, status: WithdrawPurgeStatus): List<WithdrawRequest> {
        val cutoffCreatedDate = calCutoffCreatedDate(targetTime)
        return withdrawRequestRepository.findAllByPurgeStatusAndCreatedDateLessThanEqual(
            status.value,
            cutoffCreatedDate,
            pageable
        )
    }

    fun getCntPendingRequests(targetTime: LocalDateTime, status: WithdrawPurgeStatus): Long {
        val cutoffCreatedDate = calCutoffCreatedDate(targetTime)
        return withdrawRequestRepository.countAllByPurgeStatusAndCreatedDateLessThanEqual(
            status.value,
            cutoffCreatedDate
        )
    }

    @Transactional
    fun updatePurgeStatus(old: WithdrawRequest, newStatus: WithdrawPurgeStatus, newRetryCnt: Long?) {
        old.updateStatus(newStatus)
        newRetryCnt?.let{
            old.updateTryCnt(newRetryCnt)
        }
        withdrawRequestRepository.save(old)
    }


}