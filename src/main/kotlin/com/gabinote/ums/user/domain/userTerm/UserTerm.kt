package com.gabinote.ums.user.domain.userTerm

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "user_terms")
data class UserTerm(
    @Id
    var id: ObjectId? = null,

    var userId: String,

    var termCode: String,

    var termVersion: String,

    var accepted: Boolean,

    @LastModifiedDate
    var acceptedDate: LocalDateTime? = null,

)