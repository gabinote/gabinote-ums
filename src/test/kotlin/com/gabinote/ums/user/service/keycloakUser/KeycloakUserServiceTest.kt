package com.gabinote.ums.user.service.keycloakUser

import com.gabinote.ums.common.config.KeycloakConfig
import com.gabinote.ums.common.util.exception.service.ServerError
import com.gabinote.ums.testSupport.testConfig.keycloak.TestKeycloakUtil
import com.gabinote.ums.testSupport.testConfig.keycloak.TestUser
import com.gabinote.ums.testSupport.testConfig.keycloak.UseTestKeycloak
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@UseTestKeycloak
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = [KeycloakUserService::class, KeycloakConfig::class, TestKeycloakUtil::class])
class KeycloakUserServiceTest : DescribeSpec() {
    @Autowired
    lateinit var keycloakUserService: KeycloakUserService

    @Autowired
    lateinit var testKeycloakUtil: TestKeycloakUtil

    init {
        isolationMode = IsolationMode.InstancePerTest

        beforeTest {
            testKeycloakUtil.recreateRealm()
        }

        describe("[User] KeycloakUserService Test") {
            describe("keycloakUserService.updateUserRole") {
                context("올바른 roleName과 userId가 주어지면") {
                    val validUser = TestUser.USER.sub
                    val validRoleName = "ROLE_ADMIN"
                    it("사용자의 역할이 정상적으로 변경된다") {
                        keycloakUserService.updateUserRole(validUser, validRoleName)
                        testKeycloakUtil.validationUserRole(validUser, validRoleName) shouldBe true
                    }
                }

                context("존재하지 않는 userId가 주어지면") {
                    val invalidUserId = "non-existent-user-id"
                    val validRoleName = "ROLE_ADMIN"
                    it("ServerError 예외가 발생한다") {
                        val ex = assertThrows<ServerError> {
                            keycloakUserService.updateUserRole(invalidUserId, validRoleName)
                        }
                        ex.reason shouldBe "keycloak user or role not found. userId: $invalidUserId, roleName: $validRoleName"
                    }
                }

                context("존재하지 않는 role이 주어지면") {
                    val validUser = TestUser.USER.sub
                    val invalidRoleName = "ROLE_NON_EXISTENT_ROLE"
                    it("ServerError 예외가 발생한다") {
                        val ex = assertThrows<ServerError> {
                            keycloakUserService.updateUserRole(validUser, invalidRoleName)
                        }
                        ex.reason shouldBe "keycloak user or role not found. userId: $validUser, roleName: $invalidRoleName"
                    }
                }
            }

            describe("keycloakUserService.getUserEmail") {
                context("존재하는 userId가 주어지면") {
                    val validUser = TestUser.USER.sub
                    it("사용자의 이메일을 반환한다") {
                        val email = keycloakUserService.getUserEmail(validUser)
                        email shouldNotBe null
                        email shouldContain "@"
                    }
                }

                context("존재하지 않는 userId가 주어지면") {
                    val invalidUserId = "non-existent-user-id"
                    it("ServerError 예외가 발생한다") {
                        val ex = assertThrows<ServerError> {
                            keycloakUserService.getUserEmail(invalidUserId)
                        }
                        ex.reason shouldBe "keycloak user not found. userId: $invalidUserId"
                    }
                }
            }

            describe("keycloakUserService.deleteUser") {
                context("존재하는 userId가 주어지면") {
                    val validUser = TestUser.GUEST.sub
                    it("사용자가 삭제된다") {
                        // 삭제 전 사용자 존재 확인
                        testKeycloakUtil.validationUserExist(validUser) shouldBe true

                        keycloakUserService.deleteUser(validUser)

                        // 삭제 후 사용자 존재하지 않음 확인
                        testKeycloakUtil.validationUserExist(validUser, negativeMode = true) shouldBe true
                    }
                }

                context("존재하지 않는 userId가 주어지면") {
                    val invalidUserId = "non-existent-user-id"
                    it("ServerError 예외가 발생한다") {
                        val ex = assertThrows<ServerError> {
                            keycloakUserService.deleteUser(invalidUserId)
                        }
                        ex.reason shouldBe "keycloak user not found. userId: $invalidUserId"
                    }
                }
            }

            describe("keycloakUserService.disableUser") {
                context("존재하는 userId가 주어지면") {
                    val validUser = TestUser.USER.sub
                    it("사용자가 비활성화된다") {
                        // 비활성화 전 사용자 활성화 상태 확인
                        testKeycloakUtil.validationUserEnabled(validUser) shouldBe true

                        keycloakUserService.disableUser(validUser)

                        // 비활성화 후 사용자 비활성화 상태 확인
                        testKeycloakUtil.validationUserEnabled(validUser, reverseMode = true) shouldBe true
                    }
                }

                context("존재하지 않는 userId가 주어지면") {
                    val invalidUserId = "non-existent-user-id"
                    it("ServerError 예외가 발생한다") {
                        val ex = assertThrows<ServerError> {
                            keycloakUserService.disableUser(invalidUserId)
                        }
                        ex.reason shouldBe "keycloak user not found. userId: $invalidUserId"
                    }
                }
            }
        }
    }
}