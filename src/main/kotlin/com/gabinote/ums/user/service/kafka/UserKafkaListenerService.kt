//package com.gabinote.ums.user.service.kafka
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEvent
//import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEventHelper
//import com.gabinote.ums.user.service.user.UserService
//import io.github.oshai.kotlinlogging.KotlinLogging
//import org.springframework.kafka.annotation.KafkaListener
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.kafka.support.Acknowledgment
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.util.UUID
//
//private val logger = KotlinLogging.logger {}
//
//@Service
//class UserKafkaListenerService (
//    private val objectMapper: ObjectMapper,
//    private val userService: UserService,
//    private val kafkaTemplate: KafkaTemplate<String, String>
//){
//
//    @KafkaListener(
//        topics = [UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE],
//        groupId = UserWithdrawEventHelper.USER_WITHDRAW_USER_DELETE_GROUP,
//    )
//    fun deleteUserListener(message: String, ack: Acknowledgment) {
//        logger.info { "DeleteUserListener Received user withdraw event message $message" }
//        try {
//            val uid = getUidFromMessage(message)
//            userService.processDeleteUser(uid)
//            logger.info { "Successfully processed delete user event for uid: $uid" }
//        } catch (e: Exception) {
//            logger.error(e) { "Failed to process delete user event, sending to DLQ" }
//            sendToDlq(message, e)
//        } finally {
//            ack.acknowledge()
//        }
//    }
//
//    private fun sendToDlq(message: String, exception: Exception) {
//        try {
//            kafkaTemplate.send(
//                UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE_DLQ,
//                message
//            ).get()
//            logger.warn { "Message sent to DLQ: ${UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE_DLQ}" }
//        } catch (dlqException: Exception) {
//            logger.error(dlqException) { "Failed to send message to DLQ" }
//        }
//    }
//
//    private fun getUidFromMessage(message: String): UUID {
//        val eventWrapperNode = objectMapper.readTree(message)
//        val payloadNode = eventWrapperNode.get("payload").asText()
//        val event = objectMapper.readValue(payloadNode, UserWithdrawEvent::class.java)
//        return UUID.fromString(event.uid)
//    }
//}