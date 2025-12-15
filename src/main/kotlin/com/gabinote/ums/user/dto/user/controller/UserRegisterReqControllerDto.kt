package com.gabinote.ums.user.dto.user.controller

import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.util.UUID

data class UserRegisterReqControllerDto(
    val nickname: String,
    val profileImg: String,

    @JvmField
    val isOpenProfile: Boolean,
)