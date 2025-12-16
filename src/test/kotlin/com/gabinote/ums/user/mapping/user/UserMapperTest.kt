package com.gabinote.ums.user.mapping.user

import com.gabinote.ums.testSupport.testTemplate.MockkTestTemplate
import com.gabinote.ums.testSupport.testUtil.time.TestTimeProvider
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.domain.user.User
import com.gabinote.ums.user.dto.user.controller.UserFullResControllerDto
import com.gabinote.ums.user.dto.user.controller.UserMinimalResControllerDto
import com.gabinote.ums.user.dto.user.controller.UserRegisterReqControllerDto
import com.gabinote.ums.user.dto.user.controller.UserUpdateReqControllerDto
import com.gabinote.ums.user.dto.user.service.UserRegisterReqServiceDto
import com.gabinote.ums.user.dto.user.service.UserResServiceDto
import com.gabinote.ums.user.dto.user.service.UserUpdateReqServiceDto
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import java.util.*

@ContextConfiguration(
    classes = [
        UserMapperImpl::class,
    ]
)
class UserMapperTest : MockkTestTemplate() {

    @Autowired
    lateinit var userMapper: UserMapper

    init {
        describe("[User] UserMapper Test") {

            describe("UserMapper.toResServiceDto") {
                context("User 엔티티가 주어지면,") {
                    val user = User(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        nickname = "TestUser",
                        profileImg = "https://cdn.gabinote.com/profiles/test.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = true,
                        isMarketingPushAgreed = true,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    val expected = UserResServiceDto(
                        id = user.id!!,
                        uid = UUID.fromString(user.uid!!),
                        nickname = user.nickname,
                        profileImg = user.profileImg,
                        isOpenProfile = user.isOpenProfile,
                        createdDate = user.createdDate!!,
                        modifiedDate = user.modifiedDate!!
                    )

                    it("UserResServiceDto로 변환되어야 한다.") {
                        val result = userMapper.toResServiceDto(user)

                        result shouldBe expected
                    }
                }

                context("isOpenProfile이 false인 User 엔티티가 주어지면,") {
                    val user = User(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        nickname = "PrivateUser",
                        profileImg = "",
                        isOpenProfile = false,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    val expected = UserResServiceDto(
                        id = user.id!!,
                        uid = UUID.fromString(user.uid!!),
                        nickname = user.nickname,
                        profileImg = user.profileImg,
                        isOpenProfile = user.isOpenProfile,
                        createdDate = user.createdDate!!,
                        modifiedDate = user.modifiedDate!!
                    )

                    it("isOpenProfile이 false인 UserResServiceDto로 변환되어야 한다.") {
                        val result = userMapper.toResServiceDto(user)

                        result shouldBe expected
                    }
                }
            }

            describe("UserMapper.toMinimalResControllerDto") {
                context("UserResServiceDto가 주어지면,") {
                    val dto = UserResServiceDto(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING,
                        nickname = "TestUser",
                        profileImg = "https://cdn.gabinote.com/profiles/test.png",
                        isOpenProfile = true,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    val expected = UserMinimalResControllerDto(
                        uid = dto.uid,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg
                    )

                    it("UserMinimalResControllerDto로 변환되어야 한다.") {
                        val result = userMapper.toMinimalResControllerDto(dto)

                        result shouldBe expected
                    }
                }

                context("빈 profileImg를 가진 UserResServiceDto가 주어지면,") {
                    val dto = UserResServiceDto(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING,
                        nickname = "UserWithoutImage",
                        profileImg = "",
                        isOpenProfile = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    val expected = UserMinimalResControllerDto(
                        uid = dto.uid,
                        nickname = dto.nickname,
                        profileImg = ""
                    )

                    it("빈 profileImg를 가진 UserMinimalResControllerDto로 변환되어야 한다.") {
                        val result = userMapper.toMinimalResControllerDto(dto)

                        result shouldBe expected
                    }
                }
            }

            describe("UserMapper.toFullResControllerDto") {
                context("UserResServiceDto가 주어지면,") {
                    val dto = UserResServiceDto(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING,
                        nickname = "FullUser",
                        profileImg = "https://cdn.gabinote.com/profiles/full.png",
                        isOpenProfile = true,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    val expected = UserFullResControllerDto(
                        uid = dto.uid,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        createdDate = dto.createdDate,
                        modifiedDate = dto.modifiedDate
                    )

                    it("UserFullResControllerDto로 변환되어야 한다.") {
                        val result = userMapper.toFullResControllerDto(dto)

                        result shouldBe expected
                    }
                }

                context("isOpenProfile이 false인 UserResServiceDto가 주어지면,") {
                    val dto = UserResServiceDto(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING,
                        nickname = "PrivateFullUser",
                        profileImg = "https://cdn.gabinote.com/profiles/private.png",
                        isOpenProfile = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    val expected = UserFullResControllerDto(
                        uid = dto.uid,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        createdDate = dto.createdDate,
                        modifiedDate = dto.modifiedDate
                    )

                    it("isOpenProfile이 false인 UserFullResControllerDto로 변환되어야 한다.") {
                        val result = userMapper.toFullResControllerDto(dto)

                        result shouldBe expected
                    }
                }
            }

            describe("UserMapper.toRegisterReqServiceDto") {
                context("UserRegisterReqControllerDto가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val dto = UserRegisterReqControllerDto(
                        nickname = "NewUser",
                        profileImg = "https://cdn.gabinote.com/profiles/new.png",
                        isOpenProfile = true,
                        isEssentialTermsAgreements = true,
                        isMarketingEmailAgreed = true,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        isOver14 = true
                    )

                    val expected = UserRegisterReqServiceDto(
                        uid = uid ,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        isMarketingEmailAgreed = dto.isMarketingEmailAgreed,
                        isMarketingPushAgreed = dto.isMarketingPushAgreed,
                        isNightPushAgreed = dto.isNightPushAgreed
                    )

                    it("UserRegisterReqServiceDto로 변환되어야 한다.") {
                        val result = userMapper.toRegisterReqServiceDto(dto,uid )

                        result.nickname shouldBe expected.nickname
                        result.profileImg shouldBe expected.profileImg
                        result.isOpenProfile shouldBe expected.isOpenProfile
                        result.isMarketingEmailAgreed shouldBe expected.isMarketingEmailAgreed
                        result.isMarketingPushAgreed shouldBe expected.isMarketingPushAgreed
                        result.isNightPushAgreed shouldBe expected.isNightPushAgreed
                    }
                }

                context("isOpenProfile이 false인 UserRegisterReqControllerDto가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val dto = UserRegisterReqControllerDto(
                        nickname = "PrivateNewUser",
                        profileImg = "",
                        isOpenProfile = false,
                        isEssentialTermsAgreements = true,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        isOver14 = true
                    )

                    val expected = UserRegisterReqServiceDto(
                        uid = TestUuidSource.UUID_STRING,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        isMarketingEmailAgreed = dto.isMarketingEmailAgreed,
                        isMarketingPushAgreed = dto.isMarketingPushAgreed,
                        isNightPushAgreed = dto.isNightPushAgreed
                    )

                    it("isOpenProfile이 false인 UserRegisterReqServiceDto로 변환되어야 한다.") {
                        val result = userMapper.toRegisterReqServiceDto(dto,uid )

                        result.nickname shouldBe expected.nickname
                        result.profileImg shouldBe expected.profileImg
                        result.isOpenProfile shouldBe expected.isOpenProfile
                        result.isMarketingEmailAgreed shouldBe expected.isMarketingEmailAgreed
                        result.isMarketingPushAgreed shouldBe expected.isMarketingPushAgreed
                        result.isNightPushAgreed shouldBe expected.isNightPushAgreed
                    }
                }
            }

            describe("UserMapper.toUpdateReqServiceDto") {
                context("UserUpdateReqControllerDto가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val dto = UserUpdateReqControllerDto(
                        nickname = "UpdatedUser",
                        profileImg = "https://cdn.gabinote.com/profiles/updated.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = true,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = true
                    )

                    val expected = UserUpdateReqServiceDto(
                        uid = TestUuidSource.UUID_STRING,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        isMarketingEmailAgreed = dto.isMarketingEmailAgreed,
                        isMarketingPushAgreed = dto.isMarketingPushAgreed,
                        isNightPushAgreed = dto.isNightPushAgreed
                    )

                    it("UserUpdateReqServiceDto로 변환되어야 한다.") {
                        val result = userMapper.toUpdateReqServiceDto(dto,uid)

                        result.nickname shouldBe expected.nickname
                        result.profileImg shouldBe expected.profileImg
                        result.isOpenProfile shouldBe expected.isOpenProfile
                        result.isMarketingEmailAgreed shouldBe expected.isMarketingEmailAgreed
                        result.isMarketingPushAgreed shouldBe expected.isMarketingPushAgreed
                        result.isNightPushAgreed shouldBe expected.isNightPushAgreed
                    }
                }

                context("프로필을 닫는 UserUpdateReqControllerDto가 주어지면,") {
                    val uid = TestUuidSource.UUID_STRING
                    val dto = UserUpdateReqControllerDto(
                        nickname = "ClosedProfileUser",
                        profileImg = "https://cdn.gabinote.com/profiles/closed.png",
                        isOpenProfile = false,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false
                    )

                    val expected = UserUpdateReqServiceDto(
                        uid = TestUuidSource.UUID_STRING,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        isMarketingEmailAgreed = dto.isMarketingEmailAgreed,
                        isMarketingPushAgreed = dto.isMarketingPushAgreed,
                        isNightPushAgreed = dto.isNightPushAgreed
                    )

                    it("isOpenProfile이 false인 UserUpdateReqServiceDto로 변환되어야 한다.") {
                        val result = userMapper.toUpdateReqServiceDto(dto,uid)

                        result.nickname shouldBe expected.nickname
                        result.profileImg shouldBe expected.profileImg
                        result.isOpenProfile shouldBe expected.isOpenProfile
                        result.isMarketingEmailAgreed shouldBe expected.isMarketingEmailAgreed
                        result.isMarketingPushAgreed shouldBe expected.isMarketingPushAgreed
                        result.isNightPushAgreed shouldBe expected.isNightPushAgreed
                    }
                }
            }

            describe("UserMapper.toUser") {
                context("UserRegisterReqServiceDto가 주어지면,") {
                    val dto = UserRegisterReqServiceDto(
                        uid = TestUuidSource.UUID_STRING,
                        nickname = "RegisterUser",
                        profileImg = "https://cdn.gabinote.com/profiles/register.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = true,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false
                    )

                    val expected = User(
                        id = null,
                        uid = null,
                        createdDate = null,
                        modifiedDate = null,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        isMarketingEmailAgreed = dto.isMarketingEmailAgreed,
                        isMarketingPushAgreed = dto.isMarketingPushAgreed,
                        isNightPushAgreed = dto.isNightPushAgreed
                    )

                    it("User 엔티티로 변환되어야 한다. (id, uid, dates는 null)") {
                        val result = userMapper.toUser(dto)

                        result shouldBe expected
                    }
                }

                context("isOpenProfile이 false인 UserRegisterReqServiceDto가 주어지면,") {
                    val dto = UserRegisterReqServiceDto(
                        uid = TestUuidSource.UUID_STRING,
                        nickname = "PrivateRegisterUser",
                        profileImg = "",
                        isOpenProfile = false,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false
                    )

                    val expected = User(
                        id = null,
                        uid = null,
                        createdDate = null,
                        modifiedDate = null,
                        nickname = dto.nickname,
                        profileImg = dto.profileImg,
                        isOpenProfile = dto.isOpenProfile,
                        isMarketingEmailAgreed = dto.isMarketingEmailAgreed,
                        isMarketingPushAgreed = dto.isMarketingPushAgreed,
                        isNightPushAgreed = dto.isNightPushAgreed
                    )

                    it("isOpenProfile이 false인 User 엔티티로 변환되어야 한다.") {
                        val result = userMapper.toUser(dto)

                        result shouldBe expected
                    }
                }
            }
        }
    }
}