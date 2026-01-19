package com.gabinote.ums.user.web.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.ums.testSupport.testDocs.user.UserDocsSchema
import com.gabinote.ums.testSupport.testTemplate.WebMvcTestTemplate
import com.gabinote.ums.testSupport.testUtil.data.user.UserTestDataHelper.createTestUserFullResControllerDto
import com.gabinote.ums.user.dto.user.service.UserResServiceDto
import com.gabinote.ums.user.mapping.user.UserMapper
import com.gabinote.ums.user.service.user.UserService
import com.gabinote.ums.user.service.userWithdraw.UserWithdrawService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(controllers = [UserAdminApiController::class])
class UserAdminApiControllerTest : WebMvcTestTemplate() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var userMapper: UserMapper

    @MockkBean
    private lateinit var userWithdrawService: UserWithdrawService

    private val apiPrefix = "/admin/api/v1/user"

    init {
        describe("[User] UserAdminApiController Test") {
            describe("UserAdminApiController.getUserByAdmin") {
                context("올바른 사용자 UID가 주어지면") {
                    val targetUid = UUID.randomUUID()
                    val serviceDto = mockk<UserResServiceDto>()
                    val expected = createTestUserFullResControllerDto(uid = targetUid)

                    beforeTest {
                        every { userService.getUserByUid(targetUid) } returns serviceDto
                        every { userMapper.toFullResControllerDto(serviceDto) } returns expected
                    }

                    it("사용자 정보를 조회하고, 200 OK를 응답한다") {
                        mockMvc.perform(get("$apiPrefix/{uid}", targetUid))
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andExpect(content().json(objectMapper.writeValueAsString(expected)))
                            .andDo(
                                document(
                                    "admin/user/getUserByAdmin",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Admin - User")
                                            .description("관리자 권한으로 사용자 정보 조회")
                                            .pathParameters(
                                                parameterWithName("uid").description("조회할 사용자의 UID (UUID)")
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
                            userService.getUserByUid(targetUid)
                            userMapper.toFullResControllerDto(serviceDto)
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

            describe("UserAdminApiController.runWithdrawalPurge") {
                context("관리자가 탈퇴 유저 강제 퍼지를 요청하면") {
                    beforeTest {
                        every { userWithdrawService.runForcePurgeWithdrawal() } returns Unit
                    }

                    it("강제 퍼지 이벤트를 발행하고, 200 OK를 응답한다") {
                        mockMvc.perform(post("$apiPrefix/withdraw/purge"))
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andDo(
                                document(
                                    "admin/user/runWithdrawalPurge",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Admin - User")
                                            .description("관리자 권한으로 탈퇴 유저 강제 퍼지 실행")
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            userWithdrawService.runForcePurgeWithdrawal()
                        }
                    }
                }
            }
        }
    }
}

