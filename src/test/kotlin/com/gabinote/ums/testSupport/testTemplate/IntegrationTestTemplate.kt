package com.gabinote.ums.testSupport.testTemplate

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.ums.testSupport.testConfig.common.UseTestContainers
import com.gabinote.ums.testSupport.testConfig.db.DatabaseContainerInitializer
import com.gabinote.ums.testSupport.testConfig.debezium.DebeziumContainerInitializer
import com.gabinote.ums.testSupport.testConfig.keycloak.KeycloakContainerInitializer
import com.gabinote.ums.testSupport.testConfig.keycloak.TestKeycloakUtil
import com.gabinote.ums.testSupport.testUtil.data.TestDataHelper
import com.gabinote.ums.testSupport.testUtil.debezium.TestDebeziumHelper
import com.gabinote.ums.testSupport.testUtil.kafka.TestKafkaHelper
import com.gabinote.ums.testSupport.testUtil.time.TestTimeProvider
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.test.TestCaseOrder
import io.restassured.RestAssured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    initializers = [DatabaseContainerInitializer::class,
        KeycloakContainerInitializer::class,
        DebeziumContainerInitializer::class // 반드시 순서 맨뒤로
    ]
)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@Import(
//    JacksonConfig::class,
    TestKeycloakUtil::class,
    TestDataHelper::class,
    TestUuidSource::class,
    TestTimeProvider::class,
    TestKafkaHelper::class,
    TestDebeziumHelper::class,
)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTestTemplate : FeatureSpec() {


    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var testDataHelper: TestDataHelper

    @Autowired
    lateinit var testKeycloakUtil: TestKeycloakUtil

    @Autowired
    lateinit var testKafkaHelper: TestKafkaHelper

    @Autowired
    lateinit var testDebeziumHelper: TestDebeziumHelper



    val apiPrefix: String = "/api/v1"


    fun beforeSpec() {
        RestAssured.basePath = apiPrefix
        RestAssured.port = port
    }

    override fun testCaseOrder(): TestCaseOrder = TestCaseOrder.Random

    init {
        beforeSpec {
            beforeSpec()
        }

        beforeTest{
            testKeycloakUtil.recreateRealm()
            testKafkaHelper.deleteAllTopics()
            testDebeziumHelper.deleteAllConnectors()
        }

    }
}