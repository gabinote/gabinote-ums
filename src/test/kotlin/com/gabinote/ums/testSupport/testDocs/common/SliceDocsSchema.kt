package com.gabinote.ums.testSupport.testDocs.common

import com.epages.restdocs.apispec.SimpleType
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

object SliceDocsSchema {
    val sliceResponseSchema: Array<FieldDescriptor> = arrayOf(
        fieldWithPath("is_last").description("마지막 페이지 여부"),
        fieldWithPath("content").description("페이지의 내용"),
        fieldWithPath("page").description("현재 페이지 번호"),
        fieldWithPath("size").description("페이지당 아이템 수"),
        fieldWithPath("sort_key[].key").type(SimpleType.STRING).description("정렬 키").optional(),
        fieldWithPath("sort_key[].direction").type(SimpleType.STRING).description("정렬 방향 (asc, desc)").optional()

    )

    fun toSliceSchema(
        content: Array<FieldDescriptor>,
    ): Array<FieldDescriptor> {
        return arrayOf(
            *content,
            *sliceResponseSchema
        )
    }

    fun Array<FieldDescriptor>.withSliceSchema(): Array<FieldDescriptor> {
        return toSliceSchema(content = this)
    }
}