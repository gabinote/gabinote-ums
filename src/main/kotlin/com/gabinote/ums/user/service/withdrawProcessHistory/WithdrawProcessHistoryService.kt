package com.gabinote.ums.user.service.withdrawProcessHistory

import com.gabinote.ums.user.domain.user.UserRepository
import com.gabinote.ums.user.domain.withdrawProcessHistory.WithdrawProcessHistory
import com.gabinote.ums.user.domain.withdrawProcessHistory.WithdrawProcessHistoryRepository
import com.gabinote.ums.user.event.userWithdraw.WithdrawProcess
import com.gabinote.ums.user.service.withdrawRequest.WithdrawRequestService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WithdrawProcessHistoryService(
    private val withdrawProcessHistoryRepository: WithdrawProcessHistoryRepository,
    private val withdrawRequestService: WithdrawRequestService,
){

    fun create(uid: UUID, process: WithdrawProcess,isPassed: Boolean = true) {
        val withdrawRequest = withdrawRequestService.fetchByUid(uid)
        val withdrawProcessHistory = WithdrawProcessHistory(
            uid = uid.toString(),
            requestId = withdrawRequest.id!!,
            process = process.value,
            isPassed = isPassed,
        )
        withdrawProcessHistoryRepository.save(withdrawProcessHistory)
    }


}