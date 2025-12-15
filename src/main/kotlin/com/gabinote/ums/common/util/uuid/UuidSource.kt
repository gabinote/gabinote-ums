package com.gabinote.ums.common.util.uuid

import java.util.*


interface UuidSource {
    fun generateUuid(): UUID
}