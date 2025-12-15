package com.gabinote.ums.testSupport.testUtil.uuid

import com.gabinote.ums.common.util.uuid.UuidSource
import org.springframework.boot.test.context.TestComponent
import java.util.UUID

@TestComponent
class TestUuidSource : UuidSource {

    override fun generateUuid(): UUID {
        return UUID.fromString("00000000-0000-0000-0000-000000000000")
    }

    companion object {
        val UUID_STRING: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}