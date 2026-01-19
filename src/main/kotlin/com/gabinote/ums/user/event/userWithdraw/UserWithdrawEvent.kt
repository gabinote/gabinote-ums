package com.gabinote.ums.user.event.userWithdraw

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserWithdrawEvent(
    val uid: String
)