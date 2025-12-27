package com.gabinote.ums.outbox.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


@Document(collection = "outbox")
data class OutBox (
    @Id
    val id: ObjectId? = null,
    val eventType: String,
    val payload: String,
    @CreatedDate
    val createdAt: LocalDateTime? = null,
)