package com.gabinote.ums.testSupport.testConfig.debezium

import com.gabinote.ums.testSupport.testConfig.container.ContainerNetworkHelper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.UUID

private val logger = KotlinLogging.logger {}

class DebeziumContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {



        @JvmStatic
        val kafka = GenericContainer(
            DockerImageName.parse("confluentinc/cp-kafka:8.0.3")
        ).apply {
            withNetwork(ContainerNetworkHelper.testNetwork)
            withNetworkAliases("kafka")
            withEnv("CLUSTER_ID", "test-cluster")
            withEnv("KAFKA_NODE_ID", "1")
            withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
            withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093,EXTERNAL://0.0.0.0:9094")

            withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT")
            withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@localhost:9093")
            withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")

            withReuse(true)
            withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
            withLabel("test-container", "kafka")
            waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(1)))
        }


        @JvmStatic
        val debezium = GenericContainer(DockerImageName.parse("quay.io/debezium/connect:3.4.0.Final")).apply {
            withNetwork(ContainerNetworkHelper.testNetwork)
            withEnv("BOOTSTRAP_SERVERS", "kafka:9092")
            withEnv("CONFIG_STORAGE_TOPIC", "DEBEZIUM_CONNECT_CONFIGS")
            withEnv("OFFSET_STORAGE_TOPIC", "DEBEZIUM_CONNECT_OFFSETS")
            withEnv("STATUS_STORAGE_TOPIC", "DEBEZIUM_CONNECT_STATUSES")
            withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1")
            withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1")
            withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1")
            withEnv("CONNECT_OFFSET_STORAGE_PARTITIONS", "1")
            withEnv("CONNECT_STATUS_STORAGE_PARTITIONS", "1")
            withEnv("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
            withEnv("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
            withEnv("GROUP_ID", "debezium-00")
            withExposedPorts(8083)
            waitingFor(
                Wait.forHttp("/")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2))
            )
            dependsOn(kafka)
            withLabel("test-container", "debezium")
            withReuse(true)
        }
    }
    override fun initialize(context: ConfigurableApplicationContext) {
        runKafkaIfNeeded(context)
        runDebeziumIfNeeded(context)
    }

    private fun runKafkaIfNeeded(context: ConfigurableApplicationContext) {
        if (!kafka.isRunning) {
            runKafkaContainer(context)
        } else {
            setKafkaProperties(context)
        }
    }

    private fun setKafkaProperties(context: ConfigurableApplicationContext) {
        val kafkaPort = kafka.getMappedPort(9094)
        val kafkaUrl = "localhost:${kafkaPort}"
        logger.info { "Using existing Kafka container at $kafkaUrl" }

        TestPropertyValues.of(
            "spring.kafka.bootstrap-servers=$kafkaUrl",
            "spring.kafka.consumer.auto-offset-reset=earliest",
        ).applyTo(context.environment)
    }
    private fun runKafkaContainer(context: ConfigurableApplicationContext) {
        val kafkaPort = ContainerNetworkHelper.getAvailablePort()
        kafka.apply {
            portBindings = listOf("$kafkaPort:9094")
            withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092,EXTERNAL://localhost:${kafkaPort}")
        }
        kafka.start()
        val kafkaUrl = "localhost:${kafkaPort}"
        logger.info { "Starting Kafka container at $kafkaUrl" }

        TestPropertyValues.of(
            "spring.kafka.bootstrap-servers=$kafkaUrl",
            "spring.kafka.consumer.auto-offset-reset=earliest",
        ).applyTo(context.environment)
    }

    private fun runDebeziumIfNeeded(context: ConfigurableApplicationContext) {
        if (!debezium.isRunning) {
            runDebeziumContainer(context)
        } else {
            setDebeziumProperties(context)
        }
    }

    private fun setDebeziumProperties(context: ConfigurableApplicationContext) {
        val debeziumPort = debezium.getMappedPort(8083)
        val debeziumUrl = "http://${debezium.host}:${debeziumPort}"
        logger.info { "Using existing Debezium container at $debeziumUrl" }

        TestPropertyValues.of(
            "debezium.connect.url=$debeziumUrl",
        ).applyTo(context.environment)
    }
    private fun runDebeziumContainer(context: ConfigurableApplicationContext) {
        debezium.start()
        val debeziumUrl = "http://${debezium.host}:${debezium.getMappedPort(8083)}"
        logger.info { "Starting Debezium container at $debeziumUrl" }

        TestPropertyValues.of(
            "debezium.connect.url=$debeziumUrl",
        ).applyTo(context.environment)
    }

}