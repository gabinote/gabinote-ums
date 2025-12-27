package com.gabinote.ums.user.service.withdrawProcessHistory

import com.gabinote.ums.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.domain.withdrawProcessHistory.WithdrawProcessHistory
import com.gabinote.ums.user.domain.withdrawProcessHistory.WithdrawProcessHistoryRepository
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import com.gabinote.ums.user.event.userWithdraw.WithdrawProcess
import com.gabinote.ums.user.service.withdrawRequest.WithdrawRequestService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId

class WithdrawProcessHistoryServiceTest : ServiceTestTemplate() {

    private lateinit var withdrawProcessHistoryService: WithdrawProcessHistoryService

    @MockK
    private lateinit var withdrawProcessHistoryRepository: WithdrawProcessHistoryRepository

    @MockK
    private lateinit var withdrawRequestService: WithdrawRequestService

    init {
        beforeTest {
            clearAllMocks()
            withdrawProcessHistoryService = WithdrawProcessHistoryService(
                withdrawProcessHistoryRepository,
                withdrawRequestService
            )
        }

        describe("[User] WithdrawProcessHistoryService Test") {
            describe("WithdrawProcessHistoryService.create") {
                context("유효한 uid와 process가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val process = WithdrawProcess.APPLICATION_USER_DELETE
                    val requestId = ObjectId()

                    val withdrawRequest = WithdrawRequest(
                        id = requestId,
                        uid = uid.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )

                    val historySlot = slot<WithdrawProcessHistory>()

                    beforeTest {
                        every {
                            withdrawRequestService.fetchByUid(uid)
                        } returns withdrawRequest

                        every {
                            withdrawProcessHistoryRepository.save(capture(historySlot))
                        } answers { historySlot.captured }
                    }

                    it("WithdrawProcessHistory를 생성하고 저장한다.") {
                        withdrawProcessHistoryService.create(uid, process)

                        verify(exactly = 1) { withdrawRequestService.fetchByUid(uid) }
                        verify(exactly = 1) { withdrawProcessHistoryRepository.save(any()) }

                        historySlot.captured.uid shouldBe uid.toString()
                        historySlot.captured.requestId shouldBe requestId
                        historySlot.captured.process shouldBe process.value
                        historySlot.captured.isPassed shouldBe true
                    }
                }

                context("isPassed가 false로 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val process = WithdrawProcess.KEYCLOAK_USER_DELETE
                    val isPassed = false
                    val requestId = ObjectId()

                    val withdrawRequest = WithdrawRequest(
                        id = requestId,
                        uid = uid.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.RETRYING.value,
                        purgeTryCnt = 1
                    )

                    val historySlot = slot<WithdrawProcessHistory>()

                    beforeTest {
                        every {
                            withdrawRequestService.fetchByUid(uid)
                        } returns withdrawRequest

                        every {
                            withdrawProcessHistoryRepository.save(capture(historySlot))
                        } answers { historySlot.captured }
                    }

                    it("isPassed가 false인 WithdrawProcessHistory를 생성하고 저장한다.") {
                        withdrawProcessHistoryService.create(uid, process, isPassed)

                        verify(exactly = 1) { withdrawRequestService.fetchByUid(uid) }
                        verify(exactly = 1) { withdrawProcessHistoryRepository.save(any()) }

                        historySlot.captured.uid shouldBe uid.toString()
                        historySlot.captured.requestId shouldBe requestId
                        historySlot.captured.process shouldBe process.value
                        historySlot.captured.isPassed shouldBe false
                    }
                }

                context("다양한 WithdrawProcess 타입이 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val requestId = ObjectId()

                    val withdrawRequest = WithdrawRequest(
                        id = requestId,
                        uid = uid.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )

                    val historySlot = slot<WithdrawProcessHistory>()

                    beforeTest {
                        every {
                            withdrawRequestService.fetchByUid(uid)
                        } returns withdrawRequest

                        every {
                            withdrawProcessHistoryRepository.save(capture(historySlot))
                        } answers { historySlot.captured }
                    }

                    it("NOTE_DELETE process로 히스토리를 생성한다.") {
                        withdrawProcessHistoryService.create(uid, WithdrawProcess.NOTE_DELETE)

                        historySlot.captured.process shouldBe WithdrawProcess.NOTE_DELETE.value
                    }

                    it("IMAGE_DELETE process로 히스토리를 생성한다.") {
                        withdrawProcessHistoryService.create(uid, WithdrawProcess.IMAGE_DELETE)

                        historySlot.captured.process shouldBe WithdrawProcess.IMAGE_DELETE.value
                    }
                }
            }
        }
    }
}

