package com.gabinote.ums.user.dto.user.service

import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.util.UUID

data class UserRegisterReqServiceDto(
    val uid : UUID,
    val nickname: String,
    val profileImg: String,

    @JvmField
    val isOpenProfile: Boolean,
)