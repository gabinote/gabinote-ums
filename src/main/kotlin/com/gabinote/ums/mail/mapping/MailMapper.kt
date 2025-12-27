package com.gabinote.ums.mail.mapping

import com.gabinote.ums.mail.dto.event.MailSendEvent
import com.gabinote.ums.mail.dto.service.MailSendReqServiceDto
import com.gabinote.ums.mail.enums.MailTemplate
import com.gabinote.ums.mail.enums.MailType
import org.mapstruct.Mapper

@Mapper(
    componentModel = "spring"
)
interface MailMapper {

    fun toEvent(dto: MailSendReqServiceDto,serviceName:String): MailSendEvent

    fun typeToString(type: MailType): String {
        return type.value
    }

    fun templateToString(template: MailTemplate): String {
        return template.value
    }
}