package com.gabinote.ums.user.web.controller

import com.gabinote.ums.user.domain.user.User
import com.gabinote.ums.user.dto.user.controller.UserFullResControllerDto
import com.gabinote.ums.user.mapping.user.UserMapper
import com.gabinote.ums.user.service.user.UserService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RequestMapping("/api/v1/admin/user")
@RestController
class UserAdminApiController(
    private val userService: UserService,
    private val userMapper: UserMapper
) {
    @GetMapping("/{uid}")
    fun getUserByAdmin(
        @PathVariable uid: UUID
    ): ResponseEntity<UserFullResControllerDto> {
        val user = userService.getUserByUid(uid)
        val res = userMapper.toFullResControllerDto(user)
        return ResponseEntity.ok(res)
    }
}