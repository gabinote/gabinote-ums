package com.gabinote.ums.user.mapping.user

import com.gabinote.ums.user.domain.user.User
import com.gabinote.ums.user.dto.user.controller.UserFullResControllerDto
import com.gabinote.ums.user.dto.user.controller.UserMinimalResControllerDto
import com.gabinote.ums.user.dto.user.controller.UserRegisterReqControllerDto
import com.gabinote.ums.user.dto.user.controller.UserUpdateReqControllerDto
import com.gabinote.ums.user.dto.user.service.UserRegisterReqServiceDto
import com.gabinote.ums.user.dto.user.service.UserResServiceDto
import com.gabinote.ums.user.dto.user.service.UserUpdateReqServiceDto
import org.mapstruct.Mapper

@Mapper(
    componentModel = "spring"
)
interface UserMapper {
    fun toResServiceDto(user: User): UserResServiceDto
    fun toMinimalResControllerDto(dto:UserResServiceDto): UserMinimalResControllerDto
    fun toFullResControllerDto(dto:UserResServiceDto): UserFullResControllerDto

    fun toRegisterReqServiceDto(dto: UserRegisterReqControllerDto): UserRegisterReqServiceDto

    fun toUpdateReqServiceDto(dto: UserUpdateReqControllerDto): UserUpdateReqServiceDto
}