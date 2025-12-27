//package com.gabinote.ums.user.integration
//
//import com.gabinote.ums.testSupport.testConfig.keycloak.TestUser
//import com.gabinote.ums.testSupport.testTemplate.IntegrationTestTemplate
//import com.gabinote.ums.testSupport.testUtil.debezium.TestDebeziumHelper
//import com.gabinote.ums.testSupport.testUtil.kafka.TestKafkaHelper
//import io.kotest.assertions.nondeterministic.eventually
//import io.kotest.matchers.shouldBe
//import io.restassured.module.kotlin.extensions.Given
//import io.restassured.module.kotlin.extensions.Then
//import io.restassured.module.kotlin.extensions.When
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.context.annotation.Import
//import kotlin.time.Duration.Companion.seconds
//
//@Import(
//    TestDebeziumHelper::class,
//    TestKafkaHelper::class
//)
//class WithdrawIntegrationTest : IntegrationTestTemplate() {
//
//    @Autowired
//    private lateinit var testDebeziumHelper: TestDebeziumHelper
//
//    @Autowired
//    private lateinit var testKafkaHelper: TestKafkaHelper
//
//    init {
//
//        feature("[User] User withdraw Integration Test") {
//            scenario("유저가 회원 탈퇴를 요청하면, keycloak에서 유저가 비활성화 되고, 애플리케이션 유저 정보도 삭제된다.") {
//                testDebeziumHelper.registerConnector("testsets/debezium/mongo-outbox-connector.json")
//                testDataHelper.setData("/testsets/user/integration/withdraw-integration-before.json")
//                val target = TestUser.USER
//
//                Given {
//                    basePath(apiPrefix)
//                    accept("application/json")
//                    header("X-Token-Sub", target.sub)
//                    header("X-Token-Roles", "ROLE_USER")
//                }.When {
//                    post("/user/me/withdraw")
//                }.Then {
//                    statusCode(204)
//                }
//                eventually(10.seconds) {
//                    testDataHelper.assertData("/testsets/user/integration/withdraw-integration-after.json")
//                    testKeycloakUtil.validationUserEnabled(target.sub, true) shouldBe true
//                }
//            }
//        }
//    }
//}
//
