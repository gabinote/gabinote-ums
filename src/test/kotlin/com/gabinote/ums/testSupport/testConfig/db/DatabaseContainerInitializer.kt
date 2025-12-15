package com.gabinote.ums.testSupport.testConfig.db


import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

private val log = KotlinLogging.logger {}

class DatabaseContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        @JvmStatic
        val database = MongoDBContainer(DockerImageName.parse("mongo:8.0.13")).apply {
            withEnv("MONGO_REPLICA_SET_NAME", "rs0")
            withCommand("--bind_ip_all --replSet rs0")
            withReuse(true)
        }

    }

    override fun initialize(context: ConfigurableApplicationContext) {
        // 테스트 컨테이너 시작
        database.start()

        log.debug { "url = ${database.replicaSetUrl}" }

        // application.yml 대신 프로퍼티로 datasource 설정
        TestPropertyValues.of(
            "spring.data.mongodb.uri=${database.replicaSetUrl}",

            "spring.test.database.replace=none"
        ).applyTo(context.environment)

    }


}