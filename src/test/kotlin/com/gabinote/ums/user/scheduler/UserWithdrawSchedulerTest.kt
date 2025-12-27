package com.gabinote.ums.user.scheduler

import com.gabinote.ums.common.util.time.TimeProvider
import com.gabinote.ums.mail.service.MailService
import com.gabinote.ums.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.ums.testSupport.testUtil.time.TestTimeProvider
import com.gabinote.ums.user.domain.withdrawRequest.WithdrawPurgeStatus
import com.gabinote.ums.user.dto.userWithdraw.service.PurgeKeycloakUserResServiceDto
import com.gabinote.ums.user.service.userWithdraw.UserWithdrawService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import java.time.temporal.ChronoUnit

class UserWithdrawSchedulerTest : ServiceTestTemplate() {

    private lateinit var userWithdrawScheduler: UserWithdrawScheduler

    @MockK
    private lateinit var userWithdrawService: UserWithdrawService

    @MockK
    private lateinit var mailService: MailService

    @MockK
    private lateinit var timeProvider: TimeProvider

    init {
        beforeTest {
            clearAllMocks()
            userWithdrawScheduler = UserWithdrawScheduler(
                userWithdrawService,
                mailService,
                timeProvider
            )
        }

        describe("[User] UserWithdrawScheduler Test") {
            describe("UserWithdrawScheduler.runWithdrawalPurge") {
                context("정상적으로 실행되면,") {
                    val startTime = TestTimeProvider.testDateTime
                    val endTime = startTime.plusSeconds(10)

                    val retryingRes = PurgeKeycloakUserResServiceDto(success = 2, failed = 0, total = 2)
                    val pendingRes = PurgeKeycloakUserResServiceDto(success = 5, failed = 1, total = 6)

                    beforeTest {
                        every { timeProvider.now() } returnsMany listOf(startTime, endTime)

                        every {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 실행",
                                "$startTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 실행되었습니다."
                            )
                        } returns Unit

                        every {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.RETRYING)
                        } returns retryingRes

                        every {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.PENDING)
                        } returns pendingRes

                        every {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 완료",
                                "$endTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 정상적으로 완료되었습니다. " +
                                        "소요 시간: ${ChronoUnit.MILLIS.between(startTime, endTime)} ms" +
                                        "\n처리 결과: RETRYING Scope : ${retryingRes.success} / ${retryingRes.total} PENDING Scope : ${pendingRes.success} / ${pendingRes.total}"
                            )
                        } returns Unit
                    }

                    it("RETRYING, PENDING 순서로 purge를 실행하고 완료 알림을 보낸다.") {
                        userWithdrawScheduler.runWithdrawalPurge()

                        verify(exactly = 2) { timeProvider.now() }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 실행",
                                "$startTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 실행되었습니다."
                            )
                        }
                        verify(exactly = 1) {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.RETRYING)
                        }
                        verify(exactly = 1) {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.PENDING)
                        }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 완료",
                                "$endTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 정상적으로 완료되었습니다. " +
                                        "소요 시간: ${ChronoUnit.MILLIS.between(startTime, endTime)} ms" +
                                        "\n처리 결과: RETRYING Scope : ${retryingRes.success} / ${retryingRes.total} PENDING Scope : ${pendingRes.success} / ${pendingRes.total}"
                            )
                        }
                    }
                }

                context("RETRYING purge 중 에러가 발생하면,") {
                    val startTime = TestTimeProvider.testDateTime
                    val errorMessage = "RETRYING purge failed"
                    val exception = RuntimeException(errorMessage)

                    beforeTest {
                        every { timeProvider.now() } returns startTime

                        every {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 실행",
                                "$startTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 실행되었습니다."
                            )
                        } returns Unit

                        every {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.RETRYING)
                        } throws exception

                        every {
                            mailService.sendAdminAlert(
                                "[ERROR] UMS Keycloak Withdraw 에러 발생",
                                "Keycloak 탈퇴 유저 영구 삭제 스케줄러 실행 중 에러가 발생했습니다.\n에러 내용: $errorMessage"
                            )
                        } returns Unit
                    }

                    it("에러 알림을 보낸다.") {
                        userWithdrawScheduler.runWithdrawalPurge()

                        verify(exactly = 1) { timeProvider.now() }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 실행",
                                "$startTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 실행되었습니다."
                            )
                        }
                        verify(exactly = 1) {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.RETRYING)
                        }
                        verify(exactly = 0) {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.PENDING)
                        }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                "[ERROR] UMS Keycloak Withdraw 에러 발생",
                                "Keycloak 탈퇴 유저 영구 삭제 스케줄러 실행 중 에러가 발생했습니다.\n에러 내용: $errorMessage"
                            )
                        }
                    }
                }

                context("PENDING purge 중 에러가 발생하면,") {
                    val startTime = TestTimeProvider.testDateTime
                    val errorMessage = "PENDING purge failed"
                    val exception = RuntimeException(errorMessage)

                    val retryingRes = PurgeKeycloakUserResServiceDto(success = 2, failed = 0, total = 2)

                    beforeTest {
                        every { timeProvider.now() } returns startTime

                        every {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 실행",
                                "$startTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 실행되었습니다."
                            )
                        } returns Unit

                        every {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.RETRYING)
                        } returns retryingRes

                        every {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.PENDING)
                        } throws exception

                        every {
                            mailService.sendAdminAlert(
                                "[ERROR] UMS Keycloak Withdraw 에러 발생",
                                "Keycloak 탈퇴 유저 영구 삭제 스케줄러 실행 중 에러가 발생했습니다.\n에러 내용: $errorMessage"
                            )
                        } returns Unit
                    }

                    it("RETRYING은 처리하고 에러 알림을 보낸다.") {
                        userWithdrawScheduler.runWithdrawalPurge()

                        verify(exactly = 1) { timeProvider.now() }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                "[INFO] UMS Keycloak Withdraw 실행",
                                "$startTime 에 Keycloak 탈퇴 유저 영구 삭제 스케줄러가 실행되었습니다."
                            )
                        }
                        verify(exactly = 1) {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.RETRYING)
                        }
                        verify(exactly = 1) {
                            userWithdrawService.purgeKeycloakUsers(WithdrawPurgeStatus.PENDING)
                        }
                        verify(exactly = 1) {
                            mailService.sendAdminAlert(
                                "[ERROR] UMS Keycloak Withdraw 에러 발생",
                                "Keycloak 탈퇴 유저 영구 삭제 스케줄러 실행 중 에러가 발생했습니다.\n에러 내용: $errorMessage"
                            )
                        }
                    }
                }
            }
        }
    }
}

