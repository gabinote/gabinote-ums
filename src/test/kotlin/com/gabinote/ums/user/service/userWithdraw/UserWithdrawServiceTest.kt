package com.gabinote.ums.user.service.userWithdraw

import com.gabinote.ums.common.util.time.TimeProvider
import com.gabinote.ums.outbox.service.OutBoxService
import com.gabinote.ums.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.ums.testSupport.testUtil.time.TestTimeProvider
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import com.gabinote.ums.user.dto.userWithdraw.service.PurgeKeycloakUserResServiceDto
import com.gabinote.ums.user.event.userWithdraw.WithdrawProcess
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserService
import com.gabinote.ums.user.service.user.UserService
import com.gabinote.ums.user.service.withdrawProcessHistory.WithdrawProcessHistoryService
import com.gabinote.ums.user.service.withdrawRequest.WithdrawRequestService
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import java.time.temporal.ChronoUnit

class UserWithdrawServiceTest : ServiceTestTemplate() {

    private lateinit var userWithdrawService: UserWithdrawService

    @MockK
    private lateinit var keycloakUserService: KeycloakUserService

    @MockK
    private lateinit var withdrawRequestService: WithdrawRequestService

    @MockK
    private lateinit var withdrawProcessHistoryService: WithdrawProcessHistoryService

    @MockK
    private lateinit var outBoxService: OutBoxService

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var timeProvider: TimeProvider

    @MockK
    private lateinit var userWithdrawPurgeService: UserWithdrawPurgeService

    @MockK
    private lateinit var publisher: ApplicationEventPublisher

    init {
        beforeTest {
            clearAllMocks()
            userWithdrawService = UserWithdrawService(
                keycloakUserService,
                withdrawRequestService,
                withdrawProcessHistoryService,
                outBoxService,
                userService,
                timeProvider,
                userWithdrawPurgeService,
                publisher
            )
            // batchSize 설정
            userWithdrawService.batchSize = "10"
        }

        describe("[User] UserWithdrawService Test") {
            describe("UserWithdrawService.withdrawUser") {
                context("유효한 uid가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val withdrawRequest = mockk<WithdrawRequest>()
                    beforeTest {
                        every { userService.delete(uid) } returns Unit
                        every { withdrawRequestService.create(uid) } returns withdrawRequest

                        every {
                            withdrawProcessHistoryService.create( request = withdrawRequest,  process = WithdrawProcess.APPLICATION_USER_DELETE)
                        } returns Unit
                        every { outBoxService.createWithdrawEvent(uid) } returns Unit
                        every { keycloakUserService.disableUser(uid.toString()) } returns Unit
                    }

                    it("유저를 삭제하고 탈퇴 프로세스를 시작한다.") {
                        userWithdrawService.withdrawUser(uid)

                        verify(exactly = 1) { userService.delete(uid) }
                        verify(exactly = 1) { withdrawRequestService.create(uid) }
                        verify(exactly = 1) {
                            withdrawProcessHistoryService.create( request = withdrawRequest,  process = WithdrawProcess.APPLICATION_USER_DELETE)
                        }
                        verify(exactly = 1) { outBoxService.createWithdrawEvent(uid) }
                        verify(exactly = 1) { keycloakUserService.disableUser(uid.toString()) }
                    }
                }
            }

            describe("UserWithdrawService.purgeKeycloakUsers") {
                context("PENDING 상태의 요청이 있으면,") {
                    val targetStatus = WithdrawPurgeStatus.PENDING
                    val cutoffBaseDateTime = TestTimeProvider.testDateTime.truncatedTo(ChronoUnit.DAYS)
                    val totalItem = 1L

                    val request = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )

                    beforeTest {
                        every { timeProvider.now() } returns TestTimeProvider.testDateTime
                        every {
                            withdrawRequestService.getCntPendingRequests(cutoffBaseDateTime, targetStatus)
                        } returns totalItem

                        every {
                            withdrawRequestService.getAllPendingRequests(
                                PageRequest.of(0, 10),
                                cutoffBaseDateTime,
                                targetStatus
                            )
                        } returns listOf(request)

                        every { userWithdrawPurgeService.purgeUser(request) } returns Unit
                    }

                    it("purge 프로세스를 시작하고 결과를 반환한다.") {
                        val result = userWithdrawService.purgeKeycloakUsers(targetStatus)

                        result.total shouldBe totalItem
                        verify(exactly = 1) { timeProvider.now() }
                        verify(exactly = 1) {
                            withdrawRequestService.getCntPendingRequests(cutoffBaseDateTime, targetStatus)
                        }
                    }
                }

                context("처리할 요청이 없으면,") {
                    val targetStatus = WithdrawPurgeStatus.PENDING
                    val cutoffBaseDateTime = TestTimeProvider.testDateTime.truncatedTo(ChronoUnit.DAYS)
                    val totalItem = 0L

                    beforeTest {
                        every { timeProvider.now() } returns TestTimeProvider.testDateTime
                        every {
                            withdrawRequestService.getCntPendingRequests(cutoffBaseDateTime, targetStatus)
                        } returns totalItem
                    }

                    it("빈 결과를 반환한다.") {
                        val result = userWithdrawService.purgeKeycloakUsers(targetStatus)

                        result.total shouldBe 0L
                        result.success shouldBe 0L
                        result.failed shouldBe 0L
                        verify(exactly = 1) { timeProvider.now() }
                        verify(exactly = 1) {
                            withdrawRequestService.getCntPendingRequests(cutoffBaseDateTime, targetStatus)
                        }
                        verify(exactly = 0) {
                            withdrawRequestService.getAllPendingRequests(any(), any(), any())
                        }
                    }
                }
            }

            describe("UserWithdrawService.processPurgeKeycloakUsers") {
                context("처리할 요청이 있으면,") {
                    val targetStatus = WithdrawPurgeStatus.PENDING
                    val cutoffBaseDateTime = TestTimeProvider.testDateTime.truncatedTo(ChronoUnit.DAYS)
                    val totalItem = 2L
                    val res = PurgeKeycloakUserResServiceDto(total = totalItem)

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
                            withdrawRequestService.getAllPendingRequests(
                                PageRequest.of(0, 10),
                                cutoffBaseDateTime,
                                targetStatus
                            )
                        } returns listOf(request1, request2)

                        every { userWithdrawPurgeService.purgeUser(request1) } returns Unit
                        every { userWithdrawPurgeService.purgeUser(request2) } returns Unit
                    }

                    it("모든 요청을 처리하고 성공 카운트를 증가시킨다.") {
                        userWithdrawService.processPurgeKeycloakUsers(
                            totalItem = totalItem,
                            res = res,
                            cutoffBaseDateTime = cutoffBaseDateTime,
                            targetStatus = targetStatus
                        )

                        res.success shouldBe 2L
                        res.failed shouldBe 0L
                        verify(exactly = 1) {
                            withdrawRequestService.getAllPendingRequests(
                                PageRequest.of(0, 10),
                                cutoffBaseDateTime,
                                targetStatus
                            )
                        }
                        verify(exactly = 1) { userWithdrawPurgeService.purgeUser(request1) }
                        verify(exactly = 1) { userWithdrawPurgeService.purgeUser(request2) }
                    }
                }

                context("purge 중 실패가 발생하면,") {
                    val targetStatus = WithdrawPurgeStatus.PENDING
                    val cutoffBaseDateTime = TestTimeProvider.testDateTime.truncatedTo(ChronoUnit.DAYS)
                    val totalItem = 1L
                    val res = PurgeKeycloakUserResServiceDto(total = totalItem)

                    val request = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )

                    beforeTest {
                        every {
                            withdrawRequestService.getAllPendingRequests(
                                PageRequest.of(0, 10),
                                cutoffBaseDateTime,
                                targetStatus
                            )
                        } returns listOf(request)

                        every { userWithdrawPurgeService.purgeUser(request) } throws RuntimeException("Purge failed")
                        every { userWithdrawPurgeService.processPurgeFail(request) } returns Unit
                    }

                    it("실패 처리를 수행하고 실패 카운트를 증가시킨다.") {
                        userWithdrawService.processPurgeKeycloakUsers(
                            totalItem = totalItem,
                            res = res,
                            cutoffBaseDateTime = cutoffBaseDateTime,
                            targetStatus = targetStatus
                        )

                        res.success shouldBe 0L
                        res.failed shouldBe 1L
                        verify(exactly = 1) { userWithdrawPurgeService.purgeUser(request) }
                        verify(exactly = 1) { userWithdrawPurgeService.processPurgeFail(request) }
                    }
                }

                context("빈 요청 리스트가 반환되면,") {
                    val targetStatus = WithdrawPurgeStatus.PENDING
                    val cutoffBaseDateTime = TestTimeProvider.testDateTime.truncatedTo(ChronoUnit.DAYS)
                    val totalItem = 1L
                    val res = PurgeKeycloakUserResServiceDto(total = totalItem)

                    beforeTest {
                        every {
                            withdrawRequestService.getAllPendingRequests(
                                PageRequest.of(0, 10),
                                cutoffBaseDateTime,
                                targetStatus
                            )
                        } returns emptyList()
                    }

                    it("조기 종료한다.") {
                        userWithdrawService.processPurgeKeycloakUsers(
                            totalItem = totalItem,
                            res = res,
                            cutoffBaseDateTime = cutoffBaseDateTime,
                            targetStatus = targetStatus
                        )

                        res.success shouldBe 0L
                        res.failed shouldBe 0L
                        verify(exactly = 1) {
                            withdrawRequestService.getAllPendingRequests(
                                PageRequest.of(0, 10),
                                cutoffBaseDateTime,
                                targetStatus
                            )
                        }
                        verify(exactly = 0) { userWithdrawPurgeService.purgeUser(any()) }
                    }
                }
            }
        }
    }
}

