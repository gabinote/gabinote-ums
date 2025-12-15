package com.gabinote.ums.common.util.uuid

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*

@Profile("!test")
@Component
class DefaultUuidSource : UuidSource {
    override fun generateUuid(): UUID {
        return UUID.randomUUID()
    }
}