package com.gabinote.ums.testSupport.testDocs.user

import com.epages.restdocs.apispec.SimpleType
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

object UserDocsSchema {
    val userFullResponseSchema: Array<FieldDescriptor> = arrayOf(
        fieldWithPath("uid").type(SimpleType.STRING).description("사용자 UID (UUID)"),
        fieldWithPath("nickname").type(SimpleType.STRING).description("사용자 닉네임"),
        fieldWithPath("profile_img").type(SimpleType.STRING).description("프로필 이미지 URL"),
        fieldWithPath("is_open_profile").type(SimpleType.BOOLEAN).description("프로필 공개 여부"),
        fieldWithPath("created_date").type(SimpleType.STRING).description("생성일시"),
        fieldWithPath("modified_date").type(SimpleType.STRING).description("수정일시"),
        fieldWithPath("is_marketing_email_agreed").type(SimpleType.BOOLEAN).description("마케팅 이메일 수신 동의 여부"),
        fieldWithPath("is_marketing_push_agreed").type(SimpleType.BOOLEAN).description("마케팅 푸시 수신 동의 여부"),
        fieldWithPath("is_night_push_agreed").type(SimpleType.BOOLEAN).description("야간 푸시 수신 동의 여부")
    )

    val userMinimalResponseSchema: Array<FieldDescriptor> = arrayOf(
        fieldWithPath("uid").type(SimpleType.STRING).description("사용자 UID (UUID)"),
        fieldWithPath("nickname").type(SimpleType.STRING).description("사용자 닉네임"),
        fieldWithPath("profile_img").type(SimpleType.STRING).description("프로필 이미지 URL")
    )
}

