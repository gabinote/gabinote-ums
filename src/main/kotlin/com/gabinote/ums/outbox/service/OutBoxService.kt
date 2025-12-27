package com.gabinote.ums.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.ums.outbox.domain.OutBox
import com.gabinote.ums.outbox.domain.OutBoxRepository
import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEvent
import com.gabinote.ums.user.event.userWithdraw.UserWithdrawEventHelper
import com.gabinote.ums.user.service.user.UserService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OutBoxService(
    private val outBoxRepository: OutBoxRepository,
    private val objectMapper: ObjectMapper,
) {

    fun create(eventType: String, payload: Any) {
        val payloadString = objectMapper.writeValueAsString(payload)
        val outBox = OutBox(
            eventType = eventType,
            payload = payloadString,
        )
        outBoxRepository.save(outBox)
    }


    fun createWithdrawEvent(uid: UUID) {
        val userWithdrawEvent = UserWithdrawEvent(
            uid = uid.toString(),
        )
        create(
            eventType = UserWithdrawEventHelper.USER_WITHDRAW_EVENT_TYPE,
            payload = userWithdrawEvent,
        )
    }

}