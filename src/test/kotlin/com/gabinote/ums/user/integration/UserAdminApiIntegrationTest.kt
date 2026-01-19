package com.gabinote.ums.user.integration

import com.gabinote.ums.testSupport.testTemplate.IntegrationTestTemplate
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue

class UserAdminApiIntegrationTest : IntegrationTestTemplate() {

    override val apiPrefix: String = "/admin/api/v1"

    init {

        feature("[Admin] User Admin API Integration Test") {

            feature("[GET] /admin/api/v1/user/{uid}") {
                scenario("관리자가 사용자 UID로 정보를 조회하면, 전체 사용자 정보를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/admin-api/get-api-v1-admin-user-uid.json")
                        accept("application/json")
                        header("X-Token-Sub", "admin-user-id")
                        header("X-Token-Roles", "admin")
                    }.When {
                        get("/user/d15cdbf8-22bc-47e2-9e9a-4d171cb6522e")
                    }.Then {
                        statusCode(200)
                        body("uid", equalTo("d15cdbf8-22bc-47e2-9e9a-4d171cb6522e"))
                        body("nickname", equalTo("AdminTestUser"))
                        body("profile_img", equalTo("https://cdn.gabinote.com/profiles/admin_test.png"))
                        body("is_open_profile", equalTo(false))
                        body("is_marketing_email_agreed", equalTo(true))
                        body("is_marketing_push_agreed", equalTo(false))
                        body("is_night_push_agreed", equalTo(false))
                        body("created_date", notNullValue())
                        body("modified_date", notNullValue())
                    }
                }

                scenario("존재하지 않는 사용자 UID로 조회하면, 404 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/admin-api/get-api-v1-admin-user-uid.json")
                        accept("application/json")
                        header("X-Token-Sub", "admin-user-id")
                        header("X-Token-Roles", "admin")
                    }.When {
                        get("/user/00000000-0000-0000-0000-000000000000")
                    }.Then {
                        statusCode(404)
                    }
                }

                scenario("잘못된 UUID 형식으로 조회하면, 400 에러를 반환해야 한다.") {
                    Given {
                        testDataHelper.setData("/testsets/user/integration/admin-api/get-api-v1-admin-user-uid.json")
                        accept("application/json")
                        header("X-Token-Sub", "admin-user-id")
                        header("X-Token-Roles", "admin")
                    }.When {
                        get("/user/invalid-uuid-format")
                    }.Then {
                        statusCode(400)
                    }
                }
            }

            feature("[POST] /admin/api/v1/user/withdraw/purge") {
                scenario("관리자가 탈퇴 유저 강제 퍼지를 요청하면, 200 OK를 반환해야 한다.") {
                    Given {
                        accept("application/json")
                        header("X-Token-Sub", "admin-user-id")
                        header("X-Token-Roles", "admin")
                    }.When {
                        post("/user/withdraw/purge")
                    }.Then {
                        statusCode(200)
                    }
                }
            }
        }
    }
}
