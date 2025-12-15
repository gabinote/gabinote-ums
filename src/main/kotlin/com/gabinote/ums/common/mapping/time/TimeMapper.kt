package com.gabinote.ums.common.mapping.time

import com.gabinote.ums.common.util.time.TimeProvider
import org.mapstruct.Mapper
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.LocalDateTime

@Mapper(
    componentModel = "spring"
)
abstract class TimeMapper {
    @Autowired
    lateinit var timeProvider: TimeProvider

    fun toDateTime(epochMilli: Long): LocalDateTime {
        return Instant.ofEpochMilli(epochMilli).atZone(timeProvider.zoneId()).toLocalDateTime()
    }

    fun toEpochMilli(dateTime: LocalDateTime): Long {
        return dateTime.atZone(timeProvider.zoneId()).toInstant().toEpochMilli()
    }
}