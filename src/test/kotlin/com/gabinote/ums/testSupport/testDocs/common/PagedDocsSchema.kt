package com.gabinote.ums.testSupport.testDocs.common

import com.epages.restdocs.apispec.SimpleType
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

object PagedDocsSchema {
    val pagedResourceSchema: Array<FieldDescriptor> = arrayOf(
        fieldWithPath("page").type(SimpleType.NUMBER).description("현재 페이지 번호"),
        fieldWithPath("size").type(SimpleType.NUMBER).description("페이지 크기"),
        fieldWithPath("total_elements").type(SimpleType.NUMBER).description("총 요소 수"),
        fieldWithPath("total_pages").type(SimpleType.NUMBER).description("총 페이지 수"),
        fieldWithPath("sort_key[].key").type(SimpleType.STRING).description("정렬 키").optional(),
        fieldWithPath("sort_key[].direction").type(SimpleType.STRING).description("정렬 방향 (asc, desc)").optional()

    )

    fun toPageSchema(
        content: Array<FieldDescriptor>,
    ): Array<FieldDescriptor> {
        return arrayOf(
            *content,
            *pagedResourceSchema
        )
    }

    fun Array<FieldDescriptor>.withPageSchema(): Array<FieldDescriptor> {
        return toPageSchema(content = this)
    }


}