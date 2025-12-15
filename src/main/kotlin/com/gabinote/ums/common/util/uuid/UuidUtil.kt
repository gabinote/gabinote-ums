package com.gabinote.ums.common.util.uuid

import java.util.*

object UuidUtil {
    fun String.toUuid(): UUID {
        return UUID.fromString(this)
    }
}