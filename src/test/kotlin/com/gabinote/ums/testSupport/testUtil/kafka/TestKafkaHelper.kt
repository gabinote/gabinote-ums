package com.gabinote.ums.testSupport.testUtil.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestComponent
import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@TestComponent
class TestKafkaHelper(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    private val adminClient: AdminClient by lazy {
        val config = Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        }
        AdminClient.create(config)
    }

    fun deleteAllTopics() {
        try {

            val topicNames = adminClient.listTopics().names().get(10, TimeUnit.SECONDS)

            val topicsToDelete = topicNames.filter { topic ->
                !topic.startsWith("__") &&
                        !topic.startsWith("DEBEZIUM_") &&
                        !topic.startsWith("connect-")
            }

            if (topicsToDelete.isNotEmpty()) {

                adminClient.deleteTopics(topicsToDelete).all().get(10, TimeUnit.SECONDS)
                logger.info { "Successfully deleted Kafka topics: $topicsToDelete" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to clean Kafka topics" }

        }
    }

    fun deleteTopic(topicName: String) {
        try {
            adminClient.deleteTopics(listOf(topicName)).all().get(5, TimeUnit.SECONDS)
            logger.info { "Successfully deleted Kafka topic: $topicName" }
        } catch (e: Exception) {
            logger.warn { "Topic $topicName might not exist or failed to delete: ${e.message}" }
        }
    }

    fun sendMessage(topic: String, key: String, value: String) {
        val producerConfig = Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        }

        val producer = KafkaProducer<String, String>(producerConfig)

        val record = ProducerRecord<String, String>(topic, key, value)

        try {
            producer.send(record).get(5, TimeUnit.SECONDS)
            logger.info { "Sent message to topic $topic with key $key" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send message to topic $topic" }
        } finally {
            producer.close()
        }
    }

    /**
     * 특정 토픽에서 모든 메시지를 가져온다.
     * @param topic 토픽 이름
     * @param timeout 폴링 타임아웃 (기본 5초)
     * @return 메시지 목록 (key to value)
     */
    fun getMessages(topic: String, timeout: Duration = Duration.ofSeconds(5)): List<Pair<String?, String>> {
        val consumerProps = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-${System.currentTimeMillis()}")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_uncommitted")
        }

        val consumer = KafkaConsumer<String, String>(consumerProps)
        val messages = mutableListOf<Pair<String?, String>>()

        try {
            val partitionInfos = consumer.partitionsFor(topic)
            if (partitionInfos.isNullOrEmpty()) {
                logger.info { "No partitions found for topic $topic" }
                return messages
            }

            logger.info { "Found ${partitionInfos.size} partitions for topic $topic" }

            // 모든 파티션을 직접 할당
            val topicPartitions = partitionInfos.map { TopicPartition(it.topic(), it.partition()) }
            consumer.assign(topicPartitions)

            // 처음부터 읽기 위해 오프셋을 처음으로 이동
            consumer.seekToBeginning(topicPartitions)

            // 현재 오프셋 위치 로깅
            topicPartitions.forEach { tp ->
                val position = consumer.position(tp)
                val endOffset = consumer.endOffsets(listOf(tp))[tp] ?: 0
                logger.info { "Partition ${tp.partition()}: position=$position, endOffset=$endOffset" }
            }

            // 메시지 폴링
            val records = consumer.poll(timeout)
            logger.info { "Polled ${records.count()} records from topic $topic" }
            records.forEach { record ->
                logger.info { "Found record: partition=${record.partition()}, offset=${record.offset()}, key=${record.key()}" }
                messages.add(record.key() to record.value())
            }

            logger.info { "Retrieved ${messages.size} messages from topic $topic" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to retrieve messages from topic $topic" }
        } finally {
            consumer.close()
        }

        return messages
    }

    /**
     * 특정 토픽에 특정 메시지가 존재하는지 확인한다.
     * @param topic 토픽 이름
     * @param predicate 메시지 검증 조건
     * @param timeout 폴링 타임아웃 (기본 5초)
     * @return 조건을 만족하는 메시지가 존재하면 true
     */
    fun hasMessage(
        topic: String,
        timeout: Duration = Duration.ofSeconds(5),
        predicate: (key: String?, value: String) -> Boolean
    ): Boolean {
        val messages = getMessages(topic, timeout)
        return messages.any { (key, value) -> predicate(key, value) }
    }

    /**
     * 특정 토픽에 특정 값을 포함하는 메시지가 존재하는지 확인한다.
     * @param topic 토픽 이름
     * @param containsValue 포함되어야 하는 문자열
     * @param timeout 폴링 타임아웃 (기본 5초)
     * @return 조건을 만족하는 메시지가 존재하면 true
     */
    fun hasMessageContaining(
        topic: String,
        containsValue: String,
        timeout: Duration = Duration.ofSeconds(5)
    ): Boolean {
        return hasMessage(topic, timeout) { _, value -> value.contains(containsValue) }
    }

    /**
     * 특정 토픽에 특정 키를 가진 메시지가 존재하는지 확인한다.
     * @param topic 토픽 이름
     * @param key 메시지 키
     * @param timeout 폴링 타임아웃 (기본 5초)
     * @return 조건을 만족하는 메시지가 존재하면 true
     */
    fun hasMessageWithKey(
        topic: String,
        key: String,
        timeout: Duration = Duration.ofSeconds(5)
    ): Boolean {
        return hasMessage(topic, timeout) { msgKey, _ -> msgKey == key }
    }

    /**
     * 특정 토픽에서 조건을 만족하는 첫 번째 메시지를 가져온다.
     * @param topic 토픽 이름
     * @param predicate 메시지 검증 조건
     * @param timeout 폴링 타임아웃 (기본 5초)
     * @return 조건을 만족하는 메시지 (없으면 null)
     */
    fun findMessage(
        topic: String,
        timeout: Duration = Duration.ofSeconds(5),
        predicate: (key: String?, value: String) -> Boolean
    ): Pair<String?, String>? {
        val messages = getMessages(topic, timeout)
        return messages.firstOrNull { (key, value) -> predicate(key, value) }
    }

    /**
     * 특정 토픽의 메시지 개수를 반환한다.
     * @param topic 토픽 이름
     * @param timeout 폴링 타임아웃 (기본 5초)
     * @return 메시지 개수
     */
    fun getMessageCount(topic: String, timeout: Duration = Duration.ofSeconds(5)): Int {
        return getMessages(topic, timeout).size
    }

}