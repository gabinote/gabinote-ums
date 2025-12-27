package com.gabinote.ums.user.service.withdrawRequest

import com.gabinote.ums.common.util.exception.service.ServerError
import com.gabinote.ums.policy.domain.policy.PolicyKey
import com.gabinote.ums.policy.service.policy.PolicyService
import com.gabinote.ums.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.ums.testSupport.testUtil.page.TestPageableUtil.createPageable
import com.gabinote.ums.testSupport.testUtil.time.TestTimeProvider
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequestRepository
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserService
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserServiceTest
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.assertThrows

class WithdrawRequestServiceTest : ServiceTestTemplate() {

    private lateinit var withdrawRequestService: WithdrawRequestService

    @MockK
    private lateinit var withdrawRequestRepository: WithdrawRequestRepository

    @MockK
    private lateinit var keycloakUserService: KeycloakUserService

    @MockK
    private lateinit var policyService: PolicyService

    init {
        beforeTest {
            clearAllMocks()
            withdrawRequestService = WithdrawRequestService(
                withdrawRequestRepository,
                keycloakUserService,
                policyService
            )
        }

        describe("[User] WithdrawRequestService Test") {
            describe("WithdrawRequestService.fetchByUid") {
                context("존재하는 uid가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = uid.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )

                    beforeTest {
                        every {
                            withdrawRequestRepository.findByUid(uid.toString())
                        } returns withdrawRequest
                    }

                    it("해당 uid에 맞는 WithdrawRequest를 반환한다.") {
                        val result = withdrawRequestService.fetchByUid(uid)

                        result.uid shouldBe uid.toString()
                        verify(exactly = 1) { withdrawRequestRepository.findByUid(uid.toString()) }
                    }
                }

                context("존재하지 않는 uid가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING

                    beforeTest {
                        every {
                            withdrawRequestRepository.findByUid(uid.toString())
                        } returns null
                    }

                    it("ServerError 예외를 던진다.") {
                        val ex = assertThrows<ServerError> {
                            withdrawRequestService.fetchByUid(uid)
                        }

                        ex.errorMessage shouldBe "UserWithdraw with uid $uid not found"
                        verify(exactly = 1) { withdrawRequestRepository.findByUid(uid.toString()) }
                    }
                }
            }

            describe("WithdrawRequestService.create") {
                context("유효한 uid가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val userEmail = "test@example.com"

                    val requestSlot = slot<WithdrawRequest>()

                    beforeTest {
                        every {
                            keycloakUserService.getUserEmail(uid.toString())
                        } returns userEmail

                        every {
                            withdrawRequestRepository.save(capture(requestSlot))
                        } answers { requestSlot.captured }
                    }

                    it("WithdrawRequest를 생성하고 저장한다.") {
                        withdrawRequestService.create(uid)

                        verify(exactly = 1) { keycloakUserService.getUserEmail(uid.toString()) }
                        verify(exactly = 1) { withdrawRequestRepository.save(any()) }

                        requestSlot.captured.uid shouldBe uid.toString()
                        requestSlot.captured.email shouldBe userEmail
                        requestSlot.captured.purgeStatus shouldBe WithdrawPurgeStatus.PENDING.value
                    }
                }
            }

            describe("WithdrawRequestService.getAllPendingRequests") {
                context("유효한 파라미터가 주어지면,") {
                    val pageable = createPageable()
                    val targetTime = TestTimeProvider.testDateTime
                    val status = WithdrawPurgeStatus.PENDING
                    val cutoffDays = "7"
                    val cutoffCreatedDate = targetTime.minusDays(7)

                    val request1 = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test1@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )
                    val request2 = WithdrawRequest(
                        id = ObjectId(),
                        uid = "user-2",
                        email = "test2@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )

                    beforeTest {
                        every {
                            policyService.getByKey(PolicyKey.USER_PURGE_CUTOFF_DAYS)
                        } returns cutoffDays

                        every {
                            withdrawRequestRepository.findAllByPurgeStatusAndCreatedDateLessThanEqual(
                                status.value,
                                cutoffCreatedDate,
                                pageable
                            )
                        } returns listOf(request1, request2)
                    }

                    it("조건에 맞는 WithdrawRequest 목록을 반환한다.") {
                        val result = withdrawRequestService.getAllPendingRequests(pageable, targetTime, status)

                        result.size shouldBe 2
                        verify(exactly = 1) { policyService.getByKey(PolicyKey.USER_PURGE_CUTOFF_DAYS) }
                        verify(exactly = 1) {
                            withdrawRequestRepository.findAllByPurgeStatusAndCreatedDateLessThanEqual(
                                status.value,
                                cutoffCreatedDate,
                                pageable
                            )
                        }
                    }
                }
            }

            describe("WithdrawRequestService.getCntPendingRequests") {
                context("유효한 파라미터가 주어지면,") {
                    val targetTime = TestTimeProvider.testDateTime
                    val status = WithdrawPurgeStatus.PENDING
                    val cutoffDays = "7"
                    val cutoffCreatedDate = targetTime.minusDays(7)
                    val expectedCount = 5L

                    beforeTest {
                        every {
                            policyService.getByKey(PolicyKey.USER_PURGE_CUTOFF_DAYS)
                        } returns cutoffDays

                        every {
                            withdrawRequestRepository.countAllByPurgeStatusAndCreatedDateLessThanEqual(
                                status.value,
                                cutoffCreatedDate
                            )
                        } returns expectedCount
                    }

                    it("조건에 맞는 WithdrawRequest 개수를 반환한다.") {
                        val result = withdrawRequestService.getCntPendingRequests(targetTime, status)

                        result shouldBe expectedCount
                        verify(exactly = 1) { policyService.getByKey(PolicyKey.USER_PURGE_CUTOFF_DAYS) }
                        verify(exactly = 1) {
                            withdrawRequestRepository.countAllByPurgeStatusAndCreatedDateLessThanEqual(
                                status.value,
                                cutoffCreatedDate
                            )
                        }
                    }
                }
            }

            describe("WithdrawRequestService.updatePurgeStatus") {
                context("새 상태와 재시도 횟수가 주어지면,") {
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )
                    val newStatus = WithdrawPurgeStatus.COMPLETED
                    val newRetryCnt = 3L

                    beforeTest {
                        every {
                            withdrawRequestRepository.save(withdrawRequest)
                        } returns withdrawRequest
                    }

                    it("상태와 재시도 횟수를 업데이트하고 저장한다.") {
                        withdrawRequestService.updatePurgeStatus(withdrawRequest, newStatus, newRetryCnt)

                        withdrawRequest.purgeStatus shouldBe newStatus.value
                        withdrawRequest.purgeTryCnt shouldBe newRetryCnt
                        verify(exactly = 1) { withdrawRequestRepository.save(withdrawRequest) }
                    }
                }

                context("새 상태만 주어지고 재시도 횟수가 null이면,") {
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 1
                    )
                    val newStatus = WithdrawPurgeStatus.FAILED
                    val originalRetryCnt = withdrawRequest.purgeTryCnt

                    beforeTest {
                        every {
                            withdrawRequestRepository.save(withdrawRequest)
                        } returns withdrawRequest
                    }

                    it("상태만 업데이트하고 재시도 횟수는 유지한다.") {
                        withdrawRequestService.updatePurgeStatus(withdrawRequest, newStatus, null)

                        withdrawRequest.purgeStatus shouldBe newStatus.value
                        withdrawRequest.purgeTryCnt shouldBe originalRetryCnt
                        verify(exactly = 1) { withdrawRequestRepository.save(withdrawRequest) }
                    }
                }

                context("RETRYING 상태로 업데이트하면,") {
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )
                    val newStatus = WithdrawPurgeStatus.RETRYING
                    val newRetryCnt = 1L

                    beforeTest {
                        every {
                            withdrawRequestRepository.save(withdrawRequest)
                        } returns withdrawRequest
                    }

                    it("RETRYING 상태로 변경하고 재시도 횟수를 증가시킨다.") {
                        withdrawRequestService.updatePurgeStatus(withdrawRequest, newStatus, newRetryCnt)

                        withdrawRequest.purgeStatus shouldBe WithdrawPurgeStatus.RETRYING.value
                        withdrawRequest.purgeTryCnt shouldBe newRetryCnt
                        verify(exactly = 1) { withdrawRequestRepository.save(withdrawRequest) }
                    }
                }
            }
        }
    }
}

