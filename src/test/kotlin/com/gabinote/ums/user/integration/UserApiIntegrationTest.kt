package com.gabinote.ums.user.integration

import com.gabinote.ums.testSupport.testConfig.keycloak.TestUser
import com.gabinote.ums.testSupport.testTemplate.IntegrationTestTemplate
import com.gabinote.ums.testSupport.testUtil.json.jsonBuilder
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEventHelper
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class UserApiIntegrationTest : IntegrationTestTemplate() {

    init {
        feature("[User] User API Integration Test") {

            feature("[GET] /api/v1/user/me") {
                scenario("인증된 사용자가 자신의 정보를 요청하면, 전체 사용자 정보를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/get-api-v1-user-me.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "4c1d15d0-0bef-4e7a-9ea9-b65d277863a0")
                        header("X-Token-Roles", "ROLE_USER")
                    }.When {
                        get("/user/me")
                    }.Then {
                        statusCode(200)
                        body("uid", equalTo("4c1d15d0-0bef-4e7a-9ea9-b65d277863a0"))
                        body("nickname", equalTo("user"))
                        body("profile_img", equalTo("https://cdn.gabinote.com/profiles/avatar_dev.png"))
                        body("is_open_profile", equalTo(true))
                        body("is_marketing_email_agreed", equalTo(false))
                        body("is_marketing_push_agreed", equalTo(true))
                        body("is_night_push_agreed", equalTo(true))
                        body("created_date", notNullValue())
                        body("modified_date", notNullValue())
                    }
                }

                scenario("존재하지 않는 사용자가 자신의 정보를 요청하면, 404 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/get-api-v1-user-me.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "00000000-0000-0000-0000-000000000000")
                        header("X-Token-Roles", "ROLE_USER")
                    }.When {
                        get("/user/me")
                    }.Then {
                        statusCode(404)
                    }
                }
            }

            feature("[GET] /api/v1/user/{uid}") {
                scenario("공개 프로필 사용자의 정보를 조회하면, 최소 정보를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/get-api-v1-user-uid-open.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "a15cdbf8-22bc-47e2-9e9a-4d171cb6522f")
                        header("X-Token-Roles", "user")
                    }.When {
                        get("/user/d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                    }.Then {
                        statusCode(200)
                        body("uid", equalTo("d15cdbf8-22bc-47e2-9e9a-4d171cb6522e"))
                        body("nickname", equalTo("OpenProfileUser"))
                        body("profile_img", equalTo("https://cdn.gabinote.com/profiles/open_user.png"))
                    }
                }

                scenario("비공개 프로필 사용자의 정보를 조회하면, 403 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/get-api-v1-user-uid-closed.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "a15cdbf8-22bc-47e2-9e9a-4d171cb6522f")
                        header("X-Token-Roles", "user")
                    }.When {
                        get("/user/d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                    }.Then {
                        statusCode(403)
                    }
                }

                scenario("존재하지 않는 사용자의 정보를 조회하면, 404 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/get-api-v1-user-uid-open.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "a15cdbf8-22bc-47e2-9e9a-4d171cb6522f")
                        header("X-Token-Roles", "user")
                    }.When {
                        get("/user/00000000-0000-0000-0000-000000000000")
                    }.Then {
                        statusCode(404)
                    }
                }
            }

            feature("[POST] /api/v1/user/me/register") {
                scenario("올바른 회원가입 요청이 주어지면, 새로운 사용자를 생성하여 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-register-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", TestUser.GUEST.sub)
                        header("X-Token-Roles", "guest")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "NewUser"
                                "profile_img" to TestUuidSource.UUID_STRING.toString()
                                "is_open_profile" to true
                                "is_essential_terms_agreements" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to false
                                "is_night_push_agreed" to false
                                "is_over14" to true
                            }
                        )
                    }.When {
                        post("/user/me/register")
                    }.Then {
                        statusCode(201)
                        testDataHelper.assertData("/testsets/user/integration/user-api/post-api-v1-user-me-register-after.json")
                        body("uid", equalTo(TestUser.GUEST.sub))
                        body("nickname", equalTo("NewUser"))
                        body("profile_img", equalTo(TestUuidSource.UUID_STRING.toString()))
                        body("is_open_profile", equalTo(true))
                        body("is_marketing_email_agreed", equalTo(true))
                        body("is_marketing_push_agreed", equalTo(false))
                        body("is_night_push_agreed", equalTo(false))

                        testKeycloakUtil.validationUserRole(
                            sub = TestUser.GUEST.sub,
                            roleName = "ROLE_USER"
                        ) shouldBe true
                    }
                }

                scenario("닉네임이 너무 짧으면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-register-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "A"
                                "profile_img" to "https://cdn.gabinote.com/profiles/new_user.png"
                                "is_open_profile" to true
                                "is_essential_terms_agreements" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to false
                                "is_night_push_agreed" to false
                                "is_over14" to true
                            }
                        )
                    }.When {
                        post("/user/me/register")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("닉네임이 너무 길면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-register-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "ThisNicknameIsWayTooLongForValidation"
                                "profile_img" to "https://cdn.gabinote.com/profiles/new_user.png"
                                "is_open_profile" to true
                                "is_essential_terms_agreements" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to false
                                "is_night_push_agreed" to false
                                "is_over14" to true
                            }
                        )
                    }.When {
                        post("/user/me/register")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("프로필 이미지 URL이 너무 길면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-register-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "NewUser"
                                "profile_img" to "https://cdn.gabinote.com/profiles/this_is_a_very_long_url_that_exceeds_the_maximum_allowed_length"
                                "is_open_profile" to true
                                "is_essential_terms_agreements" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to false
                                "is_night_push_agreed" to false
                                "is_over14" to true
                            }
                        )
                    }.When {
                        post("/user/me/register")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("필수 약관에 동의하지 않으면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-register-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "NewUser"
                                "profile_img" to "https://cdn.gabinote.com/profiles/new_user.png"
                                "is_open_profile" to true
                                "is_essential_terms_agreements" to false
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to false
                                "is_night_push_agreed" to false
                                "is_over14" to true
                            }
                        )
                    }.When {
                        post("/user/me/register")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("14세 미만이면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-register-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "NewUser"
                                "profile_img" to "https://cdn.gabinote.com/profiles/new_user.png"
                                "is_open_profile" to true
                                "is_essential_terms_agreements" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to false
                                "is_night_push_agreed" to false
                                "is_over14" to false
                            }
                        )
                    }.When {
                        post("/user/me/register")
                    }.Then {
                        statusCode(400)
                    }
                }
            }

            feature("[PUT] /api/v1/user/me") {
                scenario("올바른 정보 수정 요청이 주어지면, 사용자 정보를 수정하여 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/put-api-v1-user-me-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "UpdatedNickname"
                                "profile_img" to TestUuidSource.UUID_STRING.toString()
                                "is_open_profile" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to true
                                "is_night_push_agreed" to true
                            }
                        )
                    }.When {
                        put("/user/me")
                    }.Then {
                        statusCode(200)
                        testDataHelper.assertData("/testsets/user/integration/user-api/put-api-v1-user-me-after.json")
                        body("uid", equalTo("d15cdbf8-22bc-47e2-9e9a-4d171cb6522e"))
                        body("nickname", equalTo("UpdatedNickname"))
                        body("profile_img", equalTo(TestUuidSource.UUID_STRING.toString()))
                        body("is_open_profile", equalTo(true))
                        body("is_marketing_email_agreed", equalTo(true))
                        body("is_marketing_push_agreed", equalTo(true))
                        body("is_night_push_agreed", equalTo(true))
                    }
                }

                scenario("닉네임이 너무 짧으면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/put-api-v1-user-me-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "A"
                                "profile_img" to "https://cdn.gabinote.com/profiles/updated.png"
                                "is_open_profile" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to true
                                "is_night_push_agreed" to true
                            }
                        )
                    }.When {
                        put("/user/me")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("닉네임이 너무 길면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/put-api-v1-user-me-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "ThisNicknameIsWayTooLongForValidation"
                                "profile_img" to "https://cdn.gabinote.com/profiles/updated.png"
                                "is_open_profile" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to true
                                "is_night_push_agreed" to true
                            }
                        )
                    }.When {
                        put("/user/me")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("프로필 이미지 URL이 너무 길면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/put-api-v1-user-me-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "UpdatedNickname"
                                "profile_img" to "https://cdn.gabinote.com/profiles/this_is_a_very_long_url_that_exceeds_the_maximum_allowed_length"
                                "is_open_profile" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to true
                                "is_night_push_agreed" to true
                            }
                        )
                    }.When {
                        put("/user/me")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("존재하지 않는 사용자의 정보를 수정하려고 하면, 404 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/put-api-v1-user-me-before.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "00000000-0000-0000-0000-000000000000")
                        header("X-Token-Roles", "user")
                        contentType("application/json")
                        body(
                            jsonBuilder {
                                "nickname" to "UpdatedNickname"
                                "profile_img" to TestUuidSource.UUID_STRING.toString()
                                "is_open_profile" to true
                                "is_marketing_email_agreed" to true
                                "is_marketing_push_agreed" to true
                                "is_night_push_agreed" to true
                            }
                        )
                    }.When {
                        put("/user/me")
                    }.Then {
                        statusCode(404)
                    }
                }
            }

            feature("[POST] /api/v1/user/me/withdraw") {
                scenario("인증된 사용자가 회원 탈퇴를 요청하면, 탈퇴 처리되고 204를 반환해야 한다.") {
                    Given {
                        testDebeziumHelper.registerConnector("testsets/debezium/mongo-outbox-connector.json")
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-withdraw.json")

                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", TestUser.USER.sub)
                        header("X-Token-Roles", "user")
                    }.When {
                        post("/user/me/withdraw")
                    }.Then {
                        statusCode(204)

                    }
                    // WithdrawRequest, WithdrawProcess 가 생성되고, 애플리케이션 유저 데이터가 삭제된 상태여야 함 + 회원 탈퇴 이벤트 Outbox 메시지가 생성된 상태여야 함
                    testDataHelper.assertData("/testsets/user/integration/user-api/post-api-v1-user-me-withdraw-after.json")
                    // 탈퇴 시 keycloak 유저가 disabled 되어야하고,
                    testKeycloakUtil.validationUserEnabled(TestUser.USER.sub, true) shouldBe true
                    //  Outbox 메시지로 카프카에 회원 탈퇴 이벤트가 발행되어야함.
                    eventually(30.seconds) {
                        val messages = testKafkaHelper.getMessages(
                            UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE,
                            Duration.ofSeconds(3)
                        )
                        messages.isNotEmpty() shouldBe true
                        messages.any { it.second.contains(TestUser.USER.sub) } shouldBe true
                    }
                }

                scenario("존재하지 않는 사용자가 회원 탈퇴를 요청하면, 404 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/user-api/post-api-v1-user-me-withdraw.json")
                        basePath(apiPrefix)
                        accept("application/json")
                        header("X-Token-Sub", "00000000-0000-0000-0000-000000000000")
                        header("X-Token-Roles", "user")
                    }.When {
                        post("/user/me/withdraw")
                    }.Then {
                        statusCode(404)
                    }
                }
            }
        }
    }
}

