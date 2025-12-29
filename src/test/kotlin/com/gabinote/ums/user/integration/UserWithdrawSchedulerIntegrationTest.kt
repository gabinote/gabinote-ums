package com.gabinote.ums.user.integration

import com.gabinote.ums.testSupport.testConfig.keycloak.TestUser
import com.gabinote.ums.testSupport.testTemplate.IntegrationTestTemplate
import com.gabinote.ums.user.scheduler.UserWithdrawScheduler
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired

class UserWithdrawSchedulerIntegrationTest : IntegrationTestTemplate() {

    @Autowired
    lateinit var userWithdrawScheduler: UserWithdrawScheduler

    init {
        feature("[Scheduler] UserWithdrawScheduler Integration Test") {

            feature("runWithdrawalPurge - 완전 성공 케이스") {
                scenario("PENDING 상태의 탈퇴 요청들이 있을 때, 스케줄러가 실행되면 Keycloak 유저가 삭제되고 상태가 COMPLETED로 변경되어야 한다.") {
                    testDataHelper.setData("/testsets/user/integration/schedular/scheduler-withdraw-purge-success.json")

                    userWithdrawScheduler.runWithdrawalPurge()

                    // cutoff 범위내 요청들이 COMPLETED 상태로 변경되고, Keycloak 유저가 삭제되어야 함
                    testDataHelper.assertData("/testsets/user/integration/schedular/scheduler-withdraw-purge-success-after.json")

                    // Keycloak에서 유저가 삭제되었는지 확인
                    testKeycloakUtil.validationUserExist(TestUser.USER.sub, negativeMode = true) shouldBe true
                    testKeycloakUtil.validationUserExist(TestUser.GUEST.sub, negativeMode = true) shouldBe true
                }

                scenario("RETRYING 상태의 탈퇴 요청이 있을 때, 스케줄러가 실행되면 재시도되어 성공 처리되어야 한다.") {
                    testDataHelper.setData("/testsets/user/integration/schedular/scheduler-withdraw-purge-retrying.json")

                    userWithdrawScheduler.runWithdrawalPurge()

                    //요청이 COMPLETED 상태로 변경되고, Keycloak 유저가 삭제되어야 함
                    testDataHelper.assertData("/testsets/user/integration/schedular/scheduler-withdraw-purge-retrying-after.json")

                    // Keycloak에서 유저가 삭제되었는지 확인
                    testKeycloakUtil.validationUserExist(TestUser.USER.sub, negativeMode = true) shouldBe true
                }
            }

            feature("runWithdrawalPurge - 실패 케이스") {
                scenario("존재하지 않는 Keycloak 유저의 탈퇴 요청 시, 상태가 RETRYING으로 변경되어야 한다.") {
                    // Given: 존재하지 않는 Keycloak 유저의 PENDING 탈퇴 요청 설정
                    testDataHelper.setData("/testsets/user/integration/schedular/scheduler-withdraw-purge-fail.json")

                    // When: 스케줄러 실행
                    userWithdrawScheduler.runWithdrawalPurge()

                    // Then: 요청이 RETRYING 상태로 변경되고, purgeTryCnt가 증가해야 함
                    testDataHelper.assertData("/testsets/user/integration/schedular/scheduler-withdraw-purge-fail-after.json")
                }

                scenario("최대 재시도 횟수 초과 시, 상태가 FAILED로 변경되어야 한다.") {
                    // Given: 최대 재시도 횟수에 도달한 RETRYING 탈퇴 요청 설정
                    testDataHelper.setData("/testsets/user/integration/schedular/scheduler-withdraw-purge-max-retry.json")

                    // When: 스케줄러 실행
                    userWithdrawScheduler.runWithdrawalPurge()

                    // Then: 요청이 FAILED 상태로 변경되어야 함
                    testDataHelper.assertData("/testsets/user/integration/schedular/scheduler-withdraw-purge-max-retry-after.json")
                }
            }

            feature("runWithdrawalPurge - 빈 데이터 케이스") {
                scenario("처리할 탈퇴 요청이 없을 때, 스케줄러가 정상적으로 완료되어야 한다.") {
                    // Given: 처리할 탈퇴 요청이 없는 상태 설정
                    testDataHelper.setData("/testsets/user/integration/schedular/scheduler-withdraw-purge-empty.json")

                    // When: 스케줄러 실행 (예외 없이 정상 완료)
                    userWithdrawScheduler.runWithdrawalPurge()

                    // Then: DB 상태가 변경되지 않아야 함
                    testDataHelper.assertData("/testsets/user/integration/schedular/scheduler-withdraw-purge-empty-after.json")
                }
            }

            feature("runWithdrawalPurge - 부분 성공/실패 혼합 케이스") {
                scenario("일부 성공, 일부 실패하는 탈퇴 요청들이 있을 때, 각각 적절한 상태로 변경되어야 한다.") {
                    testDataHelper.setData("/testsets/user/integration/schedular/scheduler-withdraw-purge-mixed.json")

                    userWithdrawScheduler.runWithdrawalPurge()

                    //성공한 요청은 COMPLETED, 실패한 요청은 RETRYING 상태여야 함
                    testDataHelper.assertData("/testsets/user/integration/schedular/scheduler-withdraw-purge-mixed-after.json")

                    // 성공한 유저는 Keycloak에서 삭제되어야 함
                    testKeycloakUtil.validationUserExist(TestUser.USER.sub, negativeMode = true) shouldBe true
                }
            }
        }
    }
}

