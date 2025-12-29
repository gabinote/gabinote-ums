package com.gabinote.ums.user.domain.withdrawProcessHistory

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "withdraw_process_histories")
data class WithdrawProcessHistory(
    @Id
    var id: ObjectId? = null,

    var uid: String,

    var requestId: ObjectId,

    var isPassed: Boolean,

    @CreatedDate
    var processedAt: LocalDateTime? = null,

    var process: String,
)