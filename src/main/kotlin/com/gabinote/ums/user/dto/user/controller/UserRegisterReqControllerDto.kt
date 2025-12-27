package com.gabinote.ums.user.dto.user.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.gabinote.ums.user.dto.user.constraint.UserConstraints
import com.gabinote.ums.user.dto.userTerm.controller.UserTermAgreementsReqControllerDto
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import org.bson.types.ObjectId
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserRegisterReqControllerDto(

    @field:Length(
        min = UserConstraints.NICKNAME_MIN_LENGTH,
        max = UserConstraints.NICKNAME_MAX_LENGTH,
        message = "Nickname must be between ${UserConstraints.NICKNAME_MIN_LENGTH} and ${UserConstraints.NICKNAME_MAX_LENGTH} characters long."
    )
    val nickname: String,

    @field:Length(
        max = UserConstraints.PROFILE_IMG_MAX_LENGTH,
        message = "Profile image URL must not exceed ${UserConstraints.PROFILE_IMG_MAX_LENGTH} characters."
    )
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