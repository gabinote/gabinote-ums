package com.gabinote.ums.user.dto.user.service

import com.gabinote.ums.user.dto.userTerm.controller.UserTermAgreementsReqControllerDto
import com.gabinote.ums.user.dto.userTerm.service.UserTermAgreementsReqServiceDto
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.util.UUID

data class UserRegisterReqServiceDto(
    val uid : UUID,
    val nickname: String,
    val profileImg: String,

    @JvmField
    val isOpenProfile: Boolean,

    @JvmField
    val isMarketingEmailAgreed: Boolean,

    @JvmField
    val isMarketingPushAgreed: Boolean,

    @JvmField
    val isNightPushAgreed: Boolean,

    )