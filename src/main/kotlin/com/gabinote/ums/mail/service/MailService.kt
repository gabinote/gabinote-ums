package com.gabinote.ums.mail.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.ums.mail.dto.service.MailSendReqServiceDto
import com.gabinote.ums.mail.enums.MailTemplate
import com.gabinote.ums.mail.enums.MailType
import com.gabinote.ums.mail.mapping.MailMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

const val SERVICE_NAME = "UMS"
const val MAIL_SEND_TOPIC = "mail-send-event"
@Service
class MailService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val mailMapper: MailMapper,
    private val objectMapper: ObjectMapper
) {

    fun sendMail(req: MailSendReqServiceDto) {
        val event = mailMapper.toEvent(req,SERVICE_NAME)
        val eventString = objectMapper.writeValueAsString(event)
        kafkaTemplate.send(MAIL_SEND_TOPIC, eventString)
        logger.info { "Sent mail send event to Kafka: $event" }
    }

    fun sendAdminAlert(title:String, message:String) {
        val req = MailSendReqServiceDto(
            type = MailType.ADMIN_ALERT,
            title = title,
            contents = mapOf("message" to listOf(message)),
            template = MailTemplate.ADMIN_ALERT
        )
        sendMail(req)
    }

}