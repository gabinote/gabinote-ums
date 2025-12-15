package com.gabinote.ums.testSupport.testUtil.data


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.MongoTemplate

private val logger = KotlinLogging.logger {}

@TestComponent
class TestDataHelper(
    private val mongoTemplate: MongoTemplate,

    ) {

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        this.registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
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
        val map = objectMapper.convertValue(node, Map::class.java) as MutableMap<String, Any?>

        // _id 변환 처리
        val idNode = node.get("_id")
        if (idNode != null && idNode.has("\$oid")) {
            map["_id"] = ObjectId(idNode["\$oid"].asText())
        }

        return map
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
            else -> expected == actual
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