package com.gabinote.ums.common.util.auditor.extId

import com.gabinote.ums.common.util.uuid.UuidSource
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils

@Component
class ExternalIdListener(
    private val uuidSource: UuidSource
) : AbstractMongoEventListener<Any>() {
    override fun onBeforeConvert(event: BeforeConvertEvent<Any>) {
        val source = event.source

        ReflectionUtils.doWithFields(source.javaClass) { field ->
            if (field.isAnnotationPresent(ExternalId::class.java)) {
                // 필드 접근 가능하게 설정
                field.isAccessible = true

                // 필드 값 가져오기
                val fieldValue = field.get(source)
                if (fieldValue == null) {
                    // null인 경우에만 UUID 설정
                    field.set(source, uuidSource.generateUuid().toString())
                }
            }
        }
    }
}