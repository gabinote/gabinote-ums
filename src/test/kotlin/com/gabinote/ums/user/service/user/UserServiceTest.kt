package com.gabinote.ums.user.service.user

import com.gabinote.ums.common.util.exception.service.ForbiddenByPolicy
import com.gabinote.ums.common.util.exception.service.ResourceForbidden
import com.gabinote.ums.common.util.exception.service.ResourceNotFound
import com.gabinote.ums.policy.domain.policy.PolicyKey
import com.gabinote.ums.policy.service.policy.PolicyService
import com.gabinote.ums.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.ums.testSupport.testUtil.time.TestTimeProvider
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.domain.user.User
import com.gabinote.ums.user.domain.user.UserRepository
import com.gabinote.ums.user.dto.user.service.UserRegisterReqServiceDto
import com.gabinote.ums.user.dto.user.service.UserResServiceDto
import com.gabinote.ums.user.dto.user.service.UserUpdateReqServiceDto
import com.gabinote.ums.user.mapping.user.UserMapper
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserService
import com.gabinote.ums.user.service.keycloakUser.KeycloakUserServiceTest
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

class UserServiceTest : ServiceTestTemplate() {

    private lateinit var userService: UserService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var userMapper: UserMapper

    @MockK
    private lateinit var policyService: PolicyService

    @MockK
    private lateinit var keycloakUserService: KeycloakUserService



    init {
        beforeTest {
            clearAllMocks()
            userService = UserService(
                userRepository,
                userMapper,
                policyService,
                keycloakUserService
            )
        }

        describe("[User] UserService Test") {
            describe("UserService.fetchByUid") {
                context("존재하는 User uid가 주어지면,") {
                    val existingUid = TestUuidSource.UUID_STRING
                    val existingUser = User(
                        id = ObjectId(),
                        uid = existingUid.toString(),
                        nickname = "TestUser",
                        profileImg = "https://cdn.gabinote.com/profiles/test.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    beforeTest {
                        every {
                            userRepository.findByUid(existingUid.toString())
                        } returns existingUser
                    }

                    it("해당 uid에 맞는 User를 반환한다.") {
                        val result = userService.fetchByUid(existingUid)
                        result.uid shouldBe existingUid.toString()

                        verify(exactly = 1) { userRepository.findByUid(existingUid.toString()) }
                    }
                }

                context("존재하지 않는 User uid가 주어지면,") {
                    val nonExistingUid = UUID.randomUUID()

                    beforeTest {
                        every {
                            userRepository.findByUid(nonExistingUid.toString())
                        } returns null
                    }

                    it("ResourceNotFound 예외를 던진다.") {
                        val ex = assertThrows<ResourceNotFound> { userService.fetchByUid(nonExistingUid) }
                        ex.name shouldBe "User"
                        ex.identifier shouldBe nonExistingUid.toString()
                        ex.identifierType shouldBe "iid"
                        verify(exactly = 1) { userRepository.findByUid(nonExistingUid.toString()) }
                    }
                }
            }

            describe("UserService.getUserByUid") {
                context("존재하는 User uid가 주어지면,") {
                    val existingUid = TestUuidSource.UUID_STRING
                    val existingUser = User(
                        id = ObjectId(),
                        uid = existingUid.toString(),
                        nickname = "TestUser",
                        profileImg = "https://cdn.gabinote.com/profiles/test.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )
                    val userResServiceDto = mockk<UserResServiceDto>()

                    beforeTest {
                        every {
                            userRepository.findByUid(existingUid.toString())
                        } returns existingUser

                        every {
                            userMapper.toResServiceDto(existingUser)
                        } returns userResServiceDto
                    }

                    it("해당 uid에 맞는 UserResServiceDto를 반환한다.") {
                        val result = userService.getUserByUid(existingUid)
                        result shouldBe userResServiceDto

                        verify(exactly = 1) { userRepository.findByUid(existingUid.toString()) }
                        verify(exactly = 1) { userMapper.toResServiceDto(existingUser) }
                    }
                }
            }

            describe("UserService.getOpenProfileUserByUid") {
                context("공개 프로필을 가진 User uid가 주어지면,") {
                    val existingUid = TestUuidSource.UUID_STRING
                    val requestorUid = UUID.randomUUID()
                    val existingUser = User(
                        id = ObjectId(),
                        uid = existingUid.toString(),
                        nickname = "TestUser",
                        profileImg = "https://cdn.gabinote.com/profiles/test.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )
                    val userResServiceDto = mockk<UserResServiceDto>()

                    beforeTest {
                        every {
                            userRepository.findByUid(existingUid.toString())
                        } returns existingUser

                        every {
                            userMapper.toResServiceDto(existingUser)
                        } returns userResServiceDto
                    }

                    it("해당 uid에 맞는 UserResServiceDto를 반환한다.") {
                        val result = userService.getOpenProfileUserByUid(existingUid, requestorUid)
                        result shouldBe userResServiceDto

                        verify(exactly = 1) { userRepository.findByUid(existingUid.toString()) }
                        verify(exactly = 1) { userMapper.toResServiceDto(existingUser) }
                    }
                }

                context("비공개 프로필을 가진 User uid가 주어지면,") {
                    val existingUid = TestUuidSource.UUID_STRING
                    val requestorUid = UUID.randomUUID()
                    val existingUser = User(
                        id = ObjectId(),
                        uid = existingUid.toString(),
                        nickname = "TestUser",
                        profileImg = "https://cdn.gabinote.com/profiles/test.png",
                        isOpenProfile = false,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    beforeTest {
                        every {
                            userRepository.findByUid(existingUid.toString())
                        } returns existingUser
                    }

                    it("ResourceForbidden 예외를 던진다.") {
                        val ex = assertThrows<ResourceForbidden> {
                            userService.getOpenProfileUserByUid(existingUid, requestorUid)
                        }
                        ex.errorMessage shouldBe "${requestorUid} is forbidden to access closed profile on User Profile"

                        verify(exactly = 1) { userRepository.findByUid(existingUid.toString()) }
                    }
                }
            }

            describe("UserService.createUser") {
                describe("성공 케이스") {
                    context("올바른 정보가 주어지고 가입이 허용되면,") {
                        val registerReq = UserRegisterReqServiceDto(
                            uid = TestUuidSource.UUID_STRING,
                            nickname = "NewUser",
                            profileImg = "https://cdn.gabinote.com/profiles/new.png",
                            isOpenProfile = true,
                            isMarketingEmailAgreed = false,
                            isMarketingPushAgreed = false,
                            isNightPushAgreed = false
                        )
                        val userEntity = mockk<User>()
                        val savedUser = mockk<User>()
                        val userResServiceDto = mockk<UserResServiceDto>()
                        val baseRoleName = "test-role"

                        beforeTest {
                            // 가입 가능 확인
                            every {
                                policyService.getByKey(PolicyKey.USER_ENABLED_REGISTER)
                            } returns "true"

                            // 유저 엔티티 생성
                            every {
                                userMapper.toUser(registerReq)
                            } returns userEntity

                            // 유저 저장
                            every {
                                userRepository.save(userEntity)
                            } returns savedUser

                            // Keycloak 롤 업데이트
                            every {
                                policyService.getByKey(PolicyKey.USER_REGISTER_BASE_ROLE)
                            } returns baseRoleName

                            every {
                                keycloakUserService.updateUserRole(
                                    userId = registerReq.uid.toString(),
                                    roleName = baseRoleName
                                )
                            } returns Unit

                            // DTO 변환
                            every {
                                userMapper.toResServiceDto(savedUser)
                            } returns userResServiceDto
                        }

                        it("유저를 생성하고 반환한다.") {
                            val result = userService.createUser(registerReq)
                            result shouldBe userResServiceDto

                            verify(exactly = 1) { policyService.getByKey(PolicyKey.USER_ENABLED_REGISTER) }
                            verify(exactly = 1) { userMapper.toUser(registerReq) }
                            verify(exactly = 1) { userRepository.save(userEntity) }
                            verify(exactly = 1) { policyService.getByKey(PolicyKey.USER_REGISTER_BASE_ROLE) }
                            verify(exactly = 1) {
                                keycloakUserService.updateUserRole(
                                    userId = registerReq.uid.toString(),
                                    roleName = baseRoleName
                                )
                            }
                            verify(exactly = 1) { userMapper.toResServiceDto(savedUser) }
                        }
                    }
                }

                describe("실패 케이스") {
                    context("가입이 비활성화되어 있으면,") {
                        val registerReq = UserRegisterReqServiceDto(
                            uid = TestUuidSource.UUID_STRING,
                            nickname = "NewUser",
                            profileImg = "https://cdn.gabinote.com/profiles/new.png",
                            isOpenProfile = true,
                            isMarketingEmailAgreed = false,
                            isMarketingPushAgreed = false,
                            isNightPushAgreed = false
                        )

                        beforeTest {
                            every {
                                policyService.getByKey(PolicyKey.USER_ENABLED_REGISTER)
                            } returns "false"
                        }

                        it("ForbiddenByPolicy 예외를 던진다.") {
                            val ex = assertThrows<ForbiddenByPolicy> { userService.createUser(registerReq) }
                            ex.errorMessage shouldBe "Action forbidden by policy: User registration is disabled by policy."

                            verify(exactly = 1) { policyService.getByKey(PolicyKey.USER_ENABLED_REGISTER) }
                        }
                    }
                }
            }

            describe("UserService.updateUser") {
                context("올바른 정보가 주어지면,") {
                    val updateReq = UserUpdateReqServiceDto(
                        uid = TestUuidSource.UUID_STRING,
                        nickname = "UpdatedUser",
                        profileImg = "https://cdn.gabinote.com/profiles/updated.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = true,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = true
                    )
                    val existingUser = User(
                        id = ObjectId(),
                        uid = updateReq.uid.toString(),
                        nickname = "OldUser",
                        profileImg = "https://cdn.gabinote.com/profiles/old.png",
                        isOpenProfile = false,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )
                    val savedUser = mockk<User>()
                    val userResServiceDto = mockk<UserResServiceDto>()

                    beforeTest {
                        every {
                            userRepository.findByUid(updateReq.uid.toString())
                        } returns existingUser

                        every {
                            userMapper.updateUserFromDto(source = updateReq, target = existingUser)
                        } returns Unit

                        every {
                            userRepository.save(existingUser)
                        } returns savedUser

                        every {
                            userMapper.toResServiceDto(savedUser)
                        } returns userResServiceDto
                    }

                    it("유저를 수정하고 반환한다.") {
                        val result = userService.updateUser(updateReq)
                        result shouldBe userResServiceDto

                        verify(exactly = 1) { userRepository.findByUid(updateReq.uid.toString()) }
                        verify(exactly = 1) { userMapper.updateUserFromDto(source = updateReq, target = existingUser) }
                        verify(exactly = 1) { userRepository.save(existingUser) }
                        verify(exactly = 1) { userMapper.toResServiceDto(savedUser) }
                    }
                }
            }


            describe("UserService.delete") {
                context("존재하는 User uid가 주어지면,") {
                    val existingUid = TestUuidSource.UUID_STRING
                    val existingUser = User(
                        id = ObjectId(),
                        uid = existingUid.toString(),
                        nickname = "TestUser",
                        profileImg = "https://cdn.gabinote.com/profiles/test.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )

                    beforeTest {
                        every {
                            userRepository.findByUid(existingUid.toString())
                        } returns existingUser

                        every {
                            userRepository.delete(existingUser)
                        } returns Unit
                    }

                    it("유저를 삭제한다.") {
                        userService.delete(existingUid)

                        verify(exactly = 1) { userRepository.findByUid(existingUid.toString()) }
                        verify(exactly = 1) { userRepository.delete(existingUser) }
                    }
                }

                context("존재하지 않는 User uid가 주어지면,") {
                    val nonExistingUid = UUID.randomUUID()

                    beforeTest {
                        every {
                            userRepository.findByUid(nonExistingUid.toString())
                        } returns null
                    }

                    it("ResourceNotFound 예외를 던진다.") {
                        val ex = assertThrows<ResourceNotFound> { userService.delete(nonExistingUid) }
                        ex.name shouldBe "User"
                        ex.identifier shouldBe nonExistingUid.toString()
                        ex.identifierType shouldBe "iid"

                        verify(exactly = 1) { userRepository.findByUid(nonExistingUid.toString()) }
                    }
                }
            }

            describe("UserService.getAllUsers") {
                context("페이지 정보가 주어지면,") {
                    val pageable = PageRequest.of(0, 10)
                    val user1 = User(
                        id = ObjectId(),
                        uid = TestUuidSource.UUID_STRING.toString(),
                        nickname = "User1",
                        profileImg = "https://cdn.gabinote.com/profiles/user1.png",
                        isOpenProfile = true,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )
                    val user2 = User(
                        id = ObjectId(),
                        uid = UUID.randomUUID().toString(),
                        nickname = "User2",
                        profileImg = "https://cdn.gabinote.com/profiles/user2.png",
                        isOpenProfile = false,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        createdDate = TestTimeProvider.testDateTime,
                        modifiedDate = TestTimeProvider.testDateTime
                    )
                    val userPage = PageImpl(listOf(user1, user2), pageable, 2)
                    val userDto1 = mockk<UserResServiceDto>()
                    val userDto2 = mockk<UserResServiceDto>()

                    beforeTest {
                        every {
                            userRepository.findAll(pageable)
                        } returns userPage

                        every {
                            userMapper.toResServiceDto(user1)
                        } returns userDto1

                        every {
                            userMapper.toResServiceDto(user2)
                        } returns userDto2
                    }

                    it("모든 유저의 페이지를 반환한다.") {
                        val result = userService.getAllUsers(pageable)

                        result.content.size shouldBe 2
                        result.content[0] shouldBe userDto1
                        result.content[1] shouldBe userDto2

                        verify(exactly = 1) { userRepository.findAll(pageable) }
                        verify(exactly = 1) { userMapper.toResServiceDto(user1) }
                        verify(exactly = 1) { userMapper.toResServiceDto(user2) }
                    }
                }
            }
        }
    }
}

