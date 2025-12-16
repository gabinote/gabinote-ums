package com.gabinote.ums.user.dto.user.controller

import com.gabinote.ums.user.dto.userTerm.controller.UserTermAgreementsReqControllerDto
import jakarta.validation.constraints.AssertTrue
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.util.UUID

data class UserRegisterReqControllerDto(
    val nickname: String,
    val profileImg: String,

    @JvmField
    val isOpenProfile: Boolean,

    @JvmField
    @field:AssertTrue(message = "You must agree to the essential terms and conditions.")
    var isEssentialTermsAgreements: Boolean,

    @JvmField
    val isMarketingEmailAgreed: Boolean,

    @JvmField
    val isMarketingPushAgreed: Boolean,

    @JvmField
    val isNightPushAgreed: Boolean,

    @field:AssertTrue(message = "You must be over 14 years old to register.")
    val isOver14: Boolean,
)