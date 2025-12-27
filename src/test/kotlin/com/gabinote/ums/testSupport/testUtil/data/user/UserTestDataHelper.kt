package com.gabinote.ums.testSupport.testUtil.data.user

import com.gabinote.ums.testSupport.testUtil.time.TestTimeProvider
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.dto.user.controller.UserFullResControllerDto
import com.gabinote.ums.user.dto.user.controller.UserMinimalResControllerDto
import java.util.*

object UserTestDataHelper {
    
    fun createTestUserFullResControllerDto(
        uid: UUID = TestUuidSource.UUID_STRING,
        nickname: String = "TestUser",
        profileImg: String = "https://cdn.gabinote.com/profiles/test.png",
        isOpenProfile: Boolean = true,
        isMarketingEmailAgreed: Boolean = false,
        isMarketingPushAgreed: Boolean = false,
        isNightPushAgreed: Boolean = false
    ): UserFullResControllerDto {
        return UserFullResControllerDto(
            uid = uid,
            nickname = nickname,
            profileImg = profileImg,
            isOpenProfile = isOpenProfile,
            createdDate = TestTimeProvider.testDateTime,
            modifiedDate = TestTimeProvider.testDateTime,
            isMarketingEmailAgreed = isMarketingEmailAgreed,
            isMarketingPushAgreed = isMarketingPushAgreed,
            isNightPushAgreed = isNightPushAgreed
        )
    }

    fun createTestUserMinimalResControllerDto(
        uid: UUID = TestUuidSource.UUID_STRING,
        nickname: String = "TestUser",
        profileImg: String = "https://cdn.gabinote.com/profiles/test.png"
    ): UserMinimalResControllerDto {
        return UserMinimalResControllerDto(
            uid = uid,
            nickname = nickname,
            profileImg = profileImg
        )
    }
}

