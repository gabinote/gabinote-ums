package com.gabinote.ums.testSupport.testUtil.debezium

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets


private val logger = KotlinLogging.logger {}

@TestComponent
class TestDebeziumHelper(
    @Value("\${debezium.connect.url}")
    private val debeziumUrl: String
) {

    private val restTemplate = RestTemplate()
    private val connectorsEndpoint = "$debeziumUrl/connectors"

    fun registerConnector(configFileName: String) {
        val endpoint = "$debeziumUrl/connectors"

        try {
            val resource = ClassPathResource(configFileName)
            val jsonConfig = String(resource.inputStream.readAllBytes(), StandardCharsets.UTF_8)

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val request = HttpEntity(jsonConfig, headers)
            restTemplate.postForEntity(endpoint, request, String::class.java)

            logger.info { "Successfully registered Debezium connector: $configFileName" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to register Debezium connector" }
            throw e
        }
    }

    fun deleteAllConnectors() {
        try {
            val connectorNames = restTemplate.getForObject(connectorsEndpoint, Array<String>::class.java)
            connectorNames?.forEach { name ->
                deleteConnector(name)
            }
            logger.info { "All Debezium connectors have been deleted: ${connectorNames?.joinToString()}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete all Debezium connectors" }
            throw e
        }
    }

    fun deleteConnector(name: String) {
        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
            val entity = HttpEntity<String>(null, headers)

            restTemplate.exchange(
                "$connectorsEndpoint/$name",
                HttpMethod.DELETE,
                entity,
                String::class.java
            )
            logger.info { "Successfully deleted connector: $name" }

        } catch (e: HttpClientErrorException.NotFound) {
            logger.warn { "Connector $name not found (already deleted)" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete connector: $name" }
            throw e
        }
    }
}