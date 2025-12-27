package com.gabinote.ums.testSupport.testUtil.data


import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.Instant
import java.util.Date

private val logger = KotlinLogging.logger {}

@TestComponent
class TestDataHelper(
    private val mongoTemplate: MongoTemplate,

    ) {

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())

        val mongoModule = SimpleModule().apply {

            addSerializer(ObjectId::class.java, object : JsonSerializer<ObjectId>() {
                override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
                    gen.writeStartObject()
                    gen.writeStringField("\$oid", value.toHexString())
                    gen.writeEndObject()
                }
            })

            addSerializer(Date::class.java, object : JsonSerializer<Date>() {
                override fun serialize(value: Date, gen: JsonGenerator, serializers: SerializerProvider) {
                    gen.writeStartObject()
                    gen.writeStringField("\$date", value.toInstant().toString())
                    gen.writeEndObject()
                }
            })
        }

        registerModule(mongoModule)
    }

    fun setData(jsonFile: String) {
        val input = ClassPathResource(jsonFile).inputStream
        val root = objectMapper.readTree(input)

        root.properties().forEach { (collection, nodes) ->
            mongoTemplate.dropCollection(collection)

            nodes.forEach { doc ->
                val map = toMongoMap(doc)
                mongoTemplate.insert(map, collection)
            }
        }
    }

    private fun toMongoMap(node: JsonNode): Map<String, Any?> {
        // 기존 단순 변환 로직 제거하고 재귀 함수 호출
        // val map = objectMapper.convertValue(node, Map::class.java) as MutableMap<String, Any?>

        val converted = convertNode(node)
        return converted as? Map<String, Any?>
            ?: throw IllegalArgumentException("Root node must be an object")
    }

    /**
     * JsonNode를 재귀적으로 탐색하며 MongoDB 타입($oid, $date)으로 변환
     */
    private fun convertNode(node: JsonNode): Any? {
        return when {
            node.isObject -> {
                // 1. ObjectId 처리
                if (node.has("\$oid")) {
                    return ObjectId(node.get("\$oid").asText())
                }
                // 2. Date 처리 (Extended JSON 포맷)
                if (node.has("\$date")) {
                    val dateStr = node.get("\$date").asText()
                    // ISO 8601 문자열을 Date 객체로 변환
                    return Date.from(Instant.parse(dateStr))
                }

                // 3. 일반 객체: 필드별로 재귀 호출하여 Map 생성
                val map = mutableMapOf<String, Any?>()
                node.fields().forEachRemaining { entry ->
                    map[entry.key] = convertNode(entry.value)
                }
                return map
            }

            node.isArray -> {
                // 배열 내부 요소들도 재귀적으로 변환
                node.map { convertNode(it) }
            }

            // 기본 타입 처리
            node.isTextual -> node.asText()
            node.isBoolean -> node.asBoolean()
            node.isInt -> node.asInt()
            node.isLong -> node.asInt()
            node.isDouble -> node.asDouble()
            node.isNull -> null
            else -> node.toString()
        }
    }

    fun assertData(expectedJsonFile: String) {
        val input = ClassPathResource(expectedJsonFile).inputStream
        val expected = objectMapper.readTree(input)

        expected.properties().forEach { (collection, expectedDocs) ->
            val actualDocs = mongoTemplate.findAll(Map::class.java, collection)
            val actualTree: List<JsonNode> = objectMapper.convertValue(
                actualDocs,
                object : TypeReference<List<JsonNode>>() {}
            )

            if (expectedDocs.size() != actualTree.size) {
                throw AssertionError("Collection '$collection' size mismatch: expected=${expectedDocs.size()}, actual=${actualTree.size}")
            }

            expectedDocs.zip(actualTree).forEachIndexed { idx, (expectedDoc, actualDoc) ->
                if (!matchNode(expectedDoc, actualDoc)) {
                    throw AssertionError(
                        "Mismatch in collection '$collection' at index $idx\n" +
                                "Expected: $expectedDoc\n" +
                                "Actual:   $actualDoc"
                    )
                }
            }
        }
    }

    private fun matchNode(expected: JsonNode, actual: JsonNode): Boolean {
        return when {
            // 객체라면 필드별로 재귀 검사
            expected.isObject -> {
                expected.properties().all { (field, expVal) ->
                    val actVal = actual.get(field)
                    actVal != null && matchNode(expVal, actVal)
                }
            }
            // 모든 배열은 순서 무관하게 비교
            expected.isArray -> {
                // 배열 크기가 같아야 함
                if (expected.size() != actual.size()) return false

                // 각 expected 요소에 대해 매칭되는 actual 요소가 있는지 확인
                val actualList = actual.toList().toMutableList()

                expected.all { expElement ->
                    // 매칭되는 요소 찾기
                    val matchIndex = actualList.indexOfFirst { actElement ->
                        matchNode(expElement, actElement)
                    }

                    // 매칭되는 요소가 있으면 해당 요소 제거 (중복 매칭 방지)
                    if (matchIndex >= 0) {
                        actualList.removeAt(matchIndex)
                        true
                    } else {
                        false
                    }
                }
            }
            // 패턴 문자열 검사
            expected.isTextual -> matchPattern(expected.asText(), actual)
            else -> expected.asText() == actual.asText()
        }
    }

    private fun matchPattern(expected: String, actual: JsonNode): Boolean {
        return when {
            expected == "\$anyObject()" -> !actual.isNull
            expected.startsWith("\$anyObject(") -> {
                val size = expected.removePrefix("\$anyObject(").removeSuffix(")").toInt()
                actual.isObject && actual.size() == size
            }

            expected == "\$anyString()" -> actual.isTextual
            expected.startsWith("\$anyString(/") -> {
                val regex = expected.removePrefix("\$anyString(/").removeSuffix("/)").toRegex()
                actual.isTextual && regex.matches(actual.asText())
            }

            else -> expected == actual.asText()
        }
    }
}