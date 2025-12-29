package com.gabinote.ums.user.service.userWithdraw

import com.gabinote.ums.mail.service.MailService
import com.gabinote.ums.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import com.gabinote.ums.user.event.userWithdraw.WithdrawProcess
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserService
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserServiceTest
import com.gabinote.ums.user.service.withdrawProcessHistory.WithdrawProcessHistoryService
import com.gabinote.ums.user.service.withdrawRequest.WithdrawRequestService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.bson.types.ObjectId
import java.util.UUID

class UserWithdrawPurgeServiceTest : ServiceTestTemplate() {

    private lateinit var userWithdrawPurgeService: UserWithdrawPurgeService

    @MockK
    private lateinit var keycloakUserService: KeycloakUserService

    @MockK
    private lateinit var withdrawRequestService: WithdrawRequestService

    @MockK
    private lateinit var withdrawProcessHistoryService: WithdrawProcessHistoryService

    @MockK
    private lateinit var mailService: MailService

    init {
        beforeTest {
            clearAllMocks()
            userWithdrawPurgeService = UserWithdrawPurgeService(
                keycloakUserService,
                withdrawRequestService,
                withdrawProcessHistoryService,
                mailService
            )
            userWithdrawPurgeService.maxRetryAttempts = "3"
        }

        describe("[User] UserWithdrawPurgeService Test") {
            describe("UserWithdrawPurgeService.purgeUser") {
                context("유효한 WithdrawRequest가 주어지면,") {
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 0
                    )

                    beforeTest {
                        every {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.COMPLETED,
                                newRetryCnt = withdrawRequest.purgeTryCnt
                            )
                        } returns Unit

                        every {
                            withdrawProcessHistoryService.create(
                                UUID.fromString(withdrawRequest.uid),
                                WithdrawProcess.KEYCLOAK_USER_DELETE
                            )
                        } returns Unit

                        every {
                            keycloakUserService.deleteUser(withdrawRequest.uid)
                        } returns Unit
                    }

                    it("Keycloak 유저를 삭제하고 상태를 COMPLETED로 변경한다.") {
                        userWithdrawPurgeService.purgeUser(withdrawRequest)

                        verify(exactly = 1) {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.COMPLETED,
                                newRetryCnt = withdrawRequest.purgeTryCnt
                            )
                        }
                        verify(exactly = 1) {
                            withdrawProcessHistoryService.create(
                                UUID.fromString(withdrawRequest.uid),
                                WithdrawProcess.KEYCLOAK_USER_DELETE
                            )
                        }
                        verify(exactly = 1) {
                            keycloakUserService.deleteUser(withdrawRequest.uid)
                        }
                    }
                }
            }

            describe("UserWithdrawPurgeService.processPurgeFail") {
                context("재시도 횟수가 최대 재시도 횟수 미만이면,") {
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.PENDING.value,
                        purgeTryCnt = 1
                    )

                    beforeTest {
                        every {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.RETRYING,
                                newRetryCnt = withdrawRequest.purgeTryCnt + 1
                            )
                        } returns Unit

                        every {
                            withdrawProcessHistoryService.create(
                                uid = UUID.fromString(withdrawRequest.uid),
                                process = WithdrawProcess.KEYCLOAK_USER_DELETE,
                                isPassed = false
                            )
                        } returns Unit
                    }

                    it("상태를 RETRYING으로 변경하고 재시도 횟수를 증가시킨다.") {
                        userWithdrawPurgeService.processPurgeFail(withdrawRequest)

                        verify(exactly = 1) {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.RETRYING,
                                newRetryCnt = withdrawRequest.purgeTryCnt + 1
                            )
                        }
                        verify(exactly = 1) {
                            withdrawProcessHistoryService.create(
                                uid = UUID.fromString(withdrawRequest.uid),
                                process = WithdrawProcess.KEYCLOAK_USER_DELETE,
                                isPassed = false
                            )
                        }
                        verify(exactly = 0) {
                            mailService.sendAdminAlert(any(), any())
                        }
                    }
                }

                context("재시도 횟수가 최대 재시도 횟수 이상이면,") {
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.RETRYING.value,
                        purgeTryCnt = 2
                    )

                    beforeTest {
                        every {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.FAILED,
                                newRetryCnt = withdrawRequest.purgeTryCnt
                            )
                        } returns Unit

                        every {
                            withdrawProcessHistoryService.create(
                                uid = UUID.fromString(withdrawRequest.uid),
                                process = WithdrawProcess.KEYCLOAK_USER_DELETE,
                                isPassed = false
                            )
                        } returns Unit

                        every {
                            mailService.sendAdminAlert(
                                title = "[Critical] User Purge Failed Alert",
                                message = "User with UID ${withdrawRequest.uid} has exceeded the maximum retry attempts for user data purge."
                            )
                        } returns Unit
                    }

                    it("상태를 FAILED로 변경하고 관리자에게 알림을 보낸다.") {
                        userWithdrawPurgeService.processPurgeFail(withdrawRequest)

                        verify(exactly = 1) {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.FAILED,
                                newRetryCnt = withdrawRequest.purgeTryCnt + 1
                            )
                        }
                        verify(exactly = 1) {
                            withdrawProcessHistoryService.create(
                                uid = UUID.fromString(withdrawRequest.uid),
                                process = WithdrawProcess.KEYCLOAK_USER_DELETE,
                                isPassed = false
                            )
                        }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                title = "[Critical] User Purge Failed Alert",
                                message = "User with UID ${withdrawRequest.uid} has exceeded the maximum retry attempts for user data purge."
                            )
                        }
                    }
                }
            }

            describe("UserWithdrawPurgeService.updateStatusToFailed") {
                context("WithdrawRequest가 주어지면,") {
                    val withdrawRequest = WithdrawRequest(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        email = "test@example.com",
                        purgeStatus = WithdrawPurgeStatus.RETRYING.value,
                        purgeTryCnt = 3
                    )

                    beforeTest {
                        every {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.FAILED,
                                newRetryCnt = withdrawRequest.purgeTryCnt
                            )
                        } returns Unit

                        every {
                            withdrawProcessHistoryService.create(
                                uid = UUID.fromString(withdrawRequest.uid),
                                process = WithdrawProcess.KEYCLOAK_USER_DELETE,
                                isPassed = false
                            )
                        } returns Unit

                        every {
                            mailService.sendAdminAlert(
                                title = "[Critical] User Purge Failed Alert",
                                message = "User with UID ${withdrawRequest.uid} has exceeded the maximum retry attempts for user data purge."
                            )
                        } returns Unit
                    }

                    it("상태를 FAILED로 변경하고 히스토리를 기록하며 관리자 알림을 보낸다.") {
                        userWithdrawPurgeService.updateStatusToFailed(withdrawRequest)

                        verify(exactly = 1) {
                            withdrawRequestService.updatePurgeStatus(
                                old = withdrawRequest,
                                newStatus = WithdrawPurgeStatus.FAILED,
                                newRetryCnt = withdrawRequest.purgeTryCnt + 1
                            )
                        }
                        verify(exactly = 1) {
                            withdrawProcessHistoryService.create(
                                uid = UUID.fromString(withdrawRequest.uid),
                                process = WithdrawProcess.KEYCLOAK_USER_DELETE,
                                isPassed = false
                            )
                        }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                title = "[Critical] User Purge Failed Alert",
                                message = "User with UID ${withdrawRequest.uid} has exceeded the maximum retry attempts for user data purge."
                            )
                        }
                    }
                }
            }
        }
    }
}

