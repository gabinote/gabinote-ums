package com.gabinote.ums.user.domain.user

import com.gabinote.ums.common.util.auditor.extId.ExternalId
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id
    var id: ObjectId? = null,

    @ExternalId
    var uid: String? = null,

    @CreatedDate
    var createdDate: LocalDateTime? = null,

    @LastModifiedDate
    var modifiedDate: LocalDateTime? = null,

    var nickname: String,

    var profileImg: String,

    @JvmField
    var isOpenProfile: Boolean = true,

    @JvmField
    var isMarketingEmailAgreed: Boolean,

    @JvmField
    var isMarketingPushAgreed: Boolean,

    @JvmField
    var isNightPushAgreed: Boolean,
){
    fun changeNickname(nickname: String) {
        this.nickname = nickname
    }

    fun changeProfileImg(profileImg: String) {
        this.profileImg = profileImg
    }
    fun update(nickname: String, profileImg: String) {
        this.nickname = nickname
        this.profileImg = profileImg
    }
}
