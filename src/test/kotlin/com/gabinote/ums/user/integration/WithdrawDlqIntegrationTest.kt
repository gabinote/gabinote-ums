//package com.gabinote.ums.user.integration
//
//import com.gabinote.ums.testSupport.testConfig.db.DatabaseContainerInitializer
//import com.gabinote.ums.testSupport.testConfig.keycloak.TestUser
//import com.gabinote.ums.testSupport.testTemplate.IntegrationTestTemplate
//import com.gabinote.ums.testSupport.testUtil.debezium.TestDebeziumHelper
//import com.gabinote.ums.testSupport.testUtil.kafka.TestKafkaHelper
//import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEventHelper
//import io.kotest.assertions.nondeterministic.eventually
//import io.kotest.matchers.shouldBe
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.context.annotation.Import
//import java.time.Duration
//import kotlin.time.Duration.Companion.seconds
//
///**
// * DLQ 테스트는 MongoDB를 중지시키므로 별도 클래스로 분리
// * 이 테스트는 다른 테스트와 격리되어야 함
// */
//@Import(
//    TestDebeziumHelper::class,
//    TestKafkaHelper::class
//)
//class WithdrawDlqIntegrationTest : IntegrationTestTemplate() {
//
//    @Autowired
//    private lateinit var testKafkaHelper: TestKafkaHelper
//
//    init {
//        feature("[User] User withdraw DLQ Integration Test") {
//            scenario("처리 중 예외 발생 시, 메시지가 DLQ로 이동한다.") {
//                val target = TestUser.USER
//
//                // 장애 발생 (MongoDB 중지)
//                DatabaseContainerInitializer.database.stop()
//
//                // 회원 탈퇴 카프카 메시지 발행
//                val message = """
//                        {
//                            "payload": "{\"uid\":\"${target.sub}\"}",
//                            "id": "test-dlq-message"
//                        }
//                    """.trimIndent()
//
//                testKafkaHelper.sendMessage(
//                    topic = UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE,
//                    key = target.sub,
//                    value = message
//                )
//
//                eventually(30.seconds) {
//                    val dlqMessages = testKafkaHelper.getMessages(
//                        UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE_DLQ,
//                        Duration.ofSeconds(3)
//                    )
//                    dlqMessages.isNotEmpty() shouldBe true
//                    dlqMessages.any { it.second.contains(target.sub) } shouldBe true
//                }
//
//                // 메일 발송 확인
//                eventually(30.seconds) {
//                    val dlqMessages = testKafkaHelper.getMessages(
//                        UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE_DLQ,
//                        Duration.ofSeconds(3)
//                    )
//                    dlqMessages.isNotEmpty() shouldBe true
//                    dlqMessages.any { it.second.contains(target.sub) } shouldBe true
//                }
//            }
//        }
//    }
//}
//
