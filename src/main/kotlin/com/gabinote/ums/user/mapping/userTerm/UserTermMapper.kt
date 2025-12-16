package com.gabinote.ums.user.mapping.userTerm

import com.gabinote.ums.user.dto.userTerm.controller.UserTermAgreementsReqControllerDto
import com.gabinote.ums.user.dto.userTerm.service.UserTermAgreementsReqServiceDto
import org.mapstruct.Mapper


@Mapper(
    componentModel = "spring"
)
interface UserTermMapper {
    fun toAgreementsReqServiceDto(dto: UserTermAgreementsReqControllerDto): UserTermAgreementsReqServiceDto
}