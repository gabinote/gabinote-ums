package com.gabinote.ums.user.web.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.ums.common.util.context.UserContext
import com.gabinote.ums.testSupport.testDocs.user.UserDocsSchema
import com.gabinote.ums.testSupport.testTemplate.WebMvcTestTemplate
import com.gabinote.ums.testSupport.testUtil.data.user.UserTestDataHelper.createTestUserFullResControllerDto
import com.gabinote.ums.testSupport.testUtil.data.user.UserTestDataHelper.createTestUserMinimalResControllerDto
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.user.dto.user.controller.UserRegisterReqControllerDto
import com.gabinote.ums.user.dto.user.service.UserResServiceDto
import com.gabinote.ums.user.dto.user.controller.UserUpdateReqControllerDto
import com.gabinote.ums.user.dto.user.service.UserRegisterReqServiceDto
import com.gabinote.ums.user.dto.user.service.UserUpdateReqServiceDto
import com.gabinote.ums.user.mapping.user.UserMapper
import com.gabinote.ums.user.service.user.UserService
import com.gabinote.ums.user.service.userWithdraw.UserWithdrawService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(controllers = [UserApiController::class])
class UserApiControllerTest : WebMvcTestTemplate() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var userMapper: UserMapper

    @MockkBean
    private lateinit var userContext: UserContext

    @MockkBean
    private lateinit var userWithdrawService: UserWithdrawService

    private val apiPrefix = "/api/v1/user"

    init {
        describe("[User] UserApiController Test") {
            describe("UserApiController.getMyInfo") {
                context("인증된 사용자가 요청하면") {
                    val requestorUid = TestUuidSource.UUID_STRING
                    val serviceDto = mockk<UserResServiceDto>()
                    val expected = createTestUserFullResControllerDto()

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                        every { userService.getUserByUid(requestorUid) } returns serviceDto
                        every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                    }

                    it("내 정보를 조회하고, 200 OK를 응답한다") {
                        mockMvc.perform(get("$apiPrefix/me"))
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andExpect(content().json(objectMapper.writeValueAsString(expected)))
                            .andDo(
                                document(
                                    "user/getMyInfo",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("User")
                                            .description("내 정보 조회")
                                            .responseFields(
                                                *UserDocsSchema.userFullResponseSchema
                                            )
                                            .responseSchema(Schema("UserFullResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            userContext.uidWithUUID()
                            userService.getUserByUid(requestorUid)
                            userMapper.toFullResControllerDto(serviceDto)
                        }
                    }
                }
            }

            describe("UserApiController.getOpenProfileUserInfo") {
                context("공개 프로필 사용자 UID가 주어지면") {
                    val targetUid = UUID.randomUUID()
                    val requestorUid = TestUuidSource.UUID_STRING
                    val serviceDto = mockk<UserResServiceDto>()
                    val expected = createTestUserMinimalResControllerDto(uid = targetUid)

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                        every { userService.getOpenProfileUserByUid(targetUid, requestorUid) } returns serviceDto
                        every { userMapper.toMinimalResControllerDto(serviceDto) } returns expected
                    }

                    it("공개 프로필 사용자 정보를 조회하고, 200 OK를 응답한다") {
                        mockMvc.perform(get("$apiPrefix/{uid}", targetUid))
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andExpect(content().json(objectMapper.writeValueAsString(expected)))
                            .andDo(
                                document(
                                    "user/getOpenProfileUserInfo",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("User")
                                            .description("공개 프로필 사용자 정보 조회")
                                            .pathParameters(
                                                parameterWithName("uid").description("조회할 사용자의 UID (UUID)")
                                            )
                                            .responseFields(
                                                *UserDocsSchema.userMinimalResponseSchema
                                            )
                                            .responseSchema(Schema("UserMinimalResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            userContext.uidWithUUID()
                            userService.getOpenProfileUserByUid(targetUid, requestorUid)
                            userMapper.toMinimalResControllerDto(serviceDto)
                        }
                    }
                }

                context("UUID 형식이 아닌 uid가 주어지면") {
                    val invalidUid = "not-uuid"

                    it("400 Bad Request를 응답한다") {
                        mockMvc.perform(get("$apiPrefix/{uid}", invalidUid))
                            .andDo(print())
                            .andExpect(status().isBadRequest)
                    }
                }
            }

            describe("UserApiController.register") {
                context("올바른 요청이 주어지면") {
                    val requestorUid = TestUuidSource.UUID_STRING
                    val request = UserRegisterReqControllerDto(
                        nickname = "NewUser",
                        profileImg = "https://cdn.gabinote.com/profile.png",
                        isOpenProfile = true,
                        isEssentialTermsAgreements = true,
                        isMarketingEmailAgreed = false,
                        isMarketingPushAgreed = false,
                        isNightPushAgreed = false,
                        isOver14 = true
                    )
                    val serviceReqDto = mockk<UserRegisterReqServiceDto>()
                    val serviceDto = mockk<UserResServiceDto>()
                    val expected = createTestUserFullResControllerDto(nickname = "NewUser")

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                        every { userMapper.toRegisterReqServiceDto(request, requestorUid) } returns serviceReqDto
                        every { userService.createUser(serviceReqDto) } returns serviceDto
                        every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                    }

                    it("사용자를 등록하고, 201 Created를 응답한다") {
                        mockMvc.perform(
                            post("$apiPrefix/me/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                        )
                            .andDo(print())
                            .andExpect(status().isCreated)
                            .andExpect(content().json(objectMapper.writeValueAsString(expected)))
                            .andDo(
                                document(
                                    "user/register",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("User")
                                            .description("사용자 등록")
                                            .requestFields(
                                                fieldWithPath("nickname").description("닉네임 (2-20자)"),
                                                fieldWithPath("profile_img").description("프로필 이미지 URL (최대 36자)"),
                                                fieldWithPath("is_open_profile").description("프로필 공개 여부"),
                                                fieldWithPath("is_essential_terms_agreements").description("필수 약관 동의 여부 (반드시 true)"),
                                                fieldWithPath("is_marketing_email_agreed").description("마케팅 이메일 수신 동의 여부"),
                                                fieldWithPath("is_marketing_push_agreed").description("마케팅 푸시 수신 동의 여부"),
                                                fieldWithPath("is_night_push_agreed").description("야간 푸시 수신 동의 여부"),
                                                fieldWithPath("is_over14").description("만 14세 이상 여부 (반드시 true)")
                                            )
                                            .responseFields(
                                                *UserDocsSchema.userFullResponseSchema
                                            )
                                            .responseSchema(Schema("UserFullResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            userContext.uidWithUUID()
                            userMapper.toRegisterReqServiceDto(request, requestorUid)
                            userService.createUser(serviceReqDto)
                            userMapper.toFullResControllerDto(serviceDto)
                        }
                    }
                }

                describe("nickname 검증 테스트") {
                    val requestorUid = TestUuidSource.UUID_STRING

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                    }

                    describe("실패케이스") {
                        context("nickname이 2자 미만이면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "A",
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }

                        context("nickname이 20자를 초과하면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "a".repeat(21),
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }
                    }

                    describe("성공케이스") {
                        context("nickname이 정확히 2자면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "AB",
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )
                            val serviceReqDto = mockk<UserRegisterReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto(nickname = "AB")

                            beforeTest {
                                every { userMapper.toRegisterReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.createUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("201 Created를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isCreated)
                            }
                        }

                        context("nickname이 정확히 20자면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "a".repeat(20),
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )
                            val serviceReqDto = mockk<UserRegisterReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto(nickname = "a".repeat(20))

                            beforeTest {
                                every { userMapper.toRegisterReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.createUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("201 Created를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isCreated)
                            }
                        }
                    }
                }

                describe("profileImg 검증 테스트") {
                    val requestorUid = TestUuidSource.UUID_STRING

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                    }

                    describe("실패케이스") {
                        context("profileImg가 36자를 초과하면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "a".repeat(37),
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }
                    }

                    describe("성공케이스") {
                        context("profileImg가 정확히 36자면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "a".repeat(36),
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )
                            val serviceReqDto = mockk<UserRegisterReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto()

                            beforeTest {
                                every { userMapper.toRegisterReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.createUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("201 Created를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isCreated)
                            }
                        }

                        context("profileImg가 빈 문자열이면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "",
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )
                            val serviceReqDto = mockk<UserRegisterReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto()

                            beforeTest {
                                every { userMapper.toRegisterReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.createUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("201 Created를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isCreated)
                            }
                        }
                    }
                }

                describe("isEssentialTermsAgreements 검증 테스트") {
                    val requestorUid = TestUuidSource.UUID_STRING

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                    }

                    describe("실패케이스") {
                        context("isEssentialTermsAgreements가 false면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isEssentialTermsAgreements = false,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = true
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }
                    }
                }

                describe("isOver14 검증 테스트") {
                    val requestorUid = TestUuidSource.UUID_STRING

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                    }

                    describe("실패케이스") {
                        context("isOver14가 false면") {
                            val request = UserRegisterReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isEssentialTermsAgreements = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false,
                                isOver14 = false
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    post("$apiPrefix/me/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }
                    }
                }
            }

            describe("UserApiController.updateMyInfo") {
                context("올바른 요청이 주어지면") {
                    val requestorUid = TestUuidSource.UUID_STRING
                    val request = UserUpdateReqControllerDto(
                        nickname = "UpdatedUser",
                        profileImg = "https://cdn.gabinote.com/updated.png",
                        isOpenProfile = false,
                        isMarketingEmailAgreed = true,
                        isMarketingPushAgreed = true,
                        isNightPushAgreed = true
                    )
                    val serviceReqDto = mockk<UserUpdateReqServiceDto>()
                    val serviceDto = mockk<UserResServiceDto>()
                    val expected = createTestUserFullResControllerDto(nickname = "UpdatedUser")

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                        every { userMapper.toUpdateReqServiceDto(request, requestorUid) } returns serviceReqDto
                        every { userService.updateUser(serviceReqDto) } returns serviceDto
                        every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                    }

                    it("내 정보를 수정하고, 200 OK를 응답한다") {
                        mockMvc.perform(
                            put("$apiPrefix/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                        )
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andExpect(content().json(objectMapper.writeValueAsString(expected)))
                            .andDo(
                                document(
                                    "user/updateMyInfo",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("User")
                                            .description("내 정보 수정")
                                            .requestFields(
                                                fieldWithPath("nickname").description("닉네임 (2-20자)"),
                                                fieldWithPath("profile_img").description("프로필 이미지 URL (최대 36자)"),
                                                fieldWithPath("is_open_profile").description("프로필 공개 여부"),
                                                fieldWithPath("is_marketing_email_agreed").description("마케팅 이메일 수신 동의 여부"),
                                                fieldWithPath("is_marketing_push_agreed").description("마케팅 푸시 수신 동의 여부"),
                                                fieldWithPath("is_night_push_agreed").description("야간 푸시 수신 동의 여부")
                                            )
                                            .responseFields(
                                                *UserDocsSchema.userFullResponseSchema
                                            )
                                            .responseSchema(Schema("UserFullResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            userContext.uidWithUUID()
                            userMapper.toUpdateReqServiceDto(request, requestorUid)
                            userService.updateUser(serviceReqDto)
                            userMapper.toFullResControllerDto(serviceDto)
                        }
                    }
                }

                describe("nickname 검증 테스트") {
                    val requestorUid = TestUuidSource.UUID_STRING

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                    }

                    describe("실패케이스") {
                        context("nickname이 2자 미만이면") {
                            val request = UserUpdateReqControllerDto(
                                nickname = "A",
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    put("$apiPrefix/me")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }

                        context("nickname이 20자를 초과하면") {
                            val request = UserUpdateReqControllerDto(
                                nickname = "a".repeat(21),
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    put("$apiPrefix/me")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }
                    }

                    describe("성공케이스") {
                        context("nickname이 정확히 2자면") {
                            val request = UserUpdateReqControllerDto(
                                nickname = "AB",
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false
                            )
                            val serviceReqDto = mockk<UserUpdateReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto(nickname = "AB")

                            beforeTest {
                                every { userMapper.toUpdateReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.updateUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("200 OK를 응답한다") {
                                mockMvc.perform(
                                    put("$apiPrefix/me")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isOk)
                            }
                        }

                        context("nickname이 정확히 20자면") {
                            val request = UserUpdateReqControllerDto(
                                nickname = "a".repeat(20),
                                profileImg = "https://cdn.gabinote.com/profile.png",
                                isOpenProfile = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false
                            )
                            val serviceReqDto = mockk<UserUpdateReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto(nickname = "a".repeat(20))

                            beforeTest {
                                every { userMapper.toUpdateReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.updateUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("200 OK를 응답한다") {
                                mockMvc.perform(
                                    put("$apiPrefix/me")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isOk)
                            }
                        }
                    }
                }

                describe("profileImg 검증 테스트") {
                    val requestorUid = TestUuidSource.UUID_STRING

                    beforeTest {
                        every { userContext.uidWithUUID() } returns requestorUid
                    }

                    describe("실패케이스") {
                        context("profileImg가 36자를 초과하면") {
                            val request = UserUpdateReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "a".repeat(37),
                                isOpenProfile = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false
                            )

                            it("400 Bad Request를 응답한다") {
                                mockMvc.perform(
                                    put("$apiPrefix/me")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isBadRequest)
                            }
                        }
                    }

                    describe("성공케이스") {
                        context("profileImg가 정확히 36자면") {
                            val request = UserUpdateReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "a".repeat(36),
                                isOpenProfile = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false
                            )
                            val serviceReqDto = mockk<UserUpdateReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto()

                            beforeTest {
                                every { userMapper.toUpdateReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.updateUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("200 OK를 응답한다") {
                                mockMvc.perform(
                                    put("$apiPrefix/me")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isOk)
                            }
                        }

                        context("profileImg가 빈 문자열이면") {
                            val request = UserUpdateReqControllerDto(
                                nickname = "TestUser",
                                profileImg = "",
                                isOpenProfile = true,
                                isMarketingEmailAgreed = false,
                                isMarketingPushAgreed = false,
                                isNightPushAgreed = false
                            )
                            val serviceReqDto = mockk<UserUpdateReqServiceDto>()
                            val serviceDto = mockk<UserResServiceDto>()
                            val expected = createTestUserFullResControllerDto()

                            beforeTest {
                                every { userMapper.toUpdateReqServiceDto(request, requestorUid) } returns serviceReqDto
                                every { userService.updateUser(serviceReqDto) } returns serviceDto
                                every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                            }

                            it("200 OK를 응답한다") {
                                mockMvc.perform(
                                    put("$apiPrefix/me")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                                )
                                    .andDo(print())
                                    .andExpect(status().isOk)
                            }
                        }
                    }
                }
            }

            describe("UserApiController.withdraw") {
                describe("성공 케이스") {
                    context("인증된 사용자가 요청하면") {
                        val requestorUid = TestUuidSource.UUID_STRING

                        beforeTest {
                            every { userContext.uidWithUUID() } returns requestorUid
                            every { userWithdrawService.withdrawUser(requestorUid) } returns Unit
                        }

                        it("회원 탈퇴를 처리하고, 204 No Content를 응답한다") {
                            mockMvc.perform(post("$apiPrefix/me/withdraw"))
                                .andDo(print())
                                .andExpect(status().isNoContent)
                                .andDo(
                                    document(
                                        "user/withdraw",
                                        preprocessRequest(prettyPrint()),
                                        preprocessResponse(prettyPrint()),
                                        resource(
                                            ResourceSnippetParameters
                                                .builder()
                                                .tags("User")
                                                .description("회원 탈퇴")
                                                .build()
                                        )
                                    )
                                )

                            verify(exactly = 1) {
                                userContext.uidWithUUID()
                                userWithdrawService.withdrawUser(requestorUid)
                            }
                        }
                    }
                }
            }
        }
    }
}

