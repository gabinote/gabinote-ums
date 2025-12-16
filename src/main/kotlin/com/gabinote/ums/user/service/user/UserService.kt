package com.gabinote.ums.user.service.user

import com.gabinote.ums.common.util.exception.service.ResourceNotFound
import com.gabinote.ums.user.domain.user.User
import com.gabinote.ums.user.domain.user.UserRepository
import com.gabinote.ums.user.dto.user.service.UserRegisterReqServiceDto
import com.gabinote.ums.user.dto.user.service.UserResServiceDto
import com.gabinote.ums.user.dto.user.service.UserUpdateReqServiceDto
import com.gabinote.ums.user.mapping.user.UserMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {

    fun fetchByUid(uid: UUID): User {
        return userRepository.findByUid(uid.toString())
            ?: throw ResourceNotFound(
                name = "User",
                identifier = uid.toString(),
                identifierType = "iid"
            )
    }

    fun fetchByNickname(nickname: String): User {
        return userRepository.findByNickname(nickname)
            ?: throw ResourceNotFound(
                name = "User",
                identifier = nickname,
                identifierType = "nickname"
            )
    }

    fun getUserByUid(uid: UUID): UserResServiceDto {
        val user = fetchByUid(uid)
        return userMapper.toResServiceDto(user)
    }

    fun getUserByNickname(nickname: String): UserResServiceDto {
        val user = fetchByNickname(nickname)
        return userMapper.toResServiceDto(user)
    }

    /**
     * 유저 생성
     */
    @Transactional
    fun createUser(dto: UserRegisterReqServiceDto): UserResServiceDto {
        val user = userMapper.toUser(dto)
        val savedUser = userRepository.save(user)
        // TODO: Meilisearch 연동
        return userMapper.toResServiceDto(savedUser)
    }

    /**
     * 유저 정보 수정 (uid 기반)
     */
    @Transactional
    fun updateUser(dto: UserUpdateReqServiceDto): UserResServiceDto {
        val existingUser = userRepository.findByUid(dto.uid.toString())
            ?: throw IllegalArgumentException("User not found with uid: ${dto.uid}")

        existingUser.apply {
            nickname = dto.nickname
            profileImg = dto.profileImg
            isOpenProfile = dto.isOpenProfile
        }

        val savedUser = userRepository.save(existingUser)
        return userMapper.toResServiceDto(savedUser)
    }

    /**
     * 유저 삭제 (uid 기반)
     */
    fun deleteUser(uid: UUID) {
        val user = fetchByUid(uid)
        userRepository.delete(user)
    }

    /**
     * 모든 유저 조회
     */
    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserResServiceDto> {
        return userRepository.findAll()
            .map { userMapper.toResServiceDto(it) }
    }



}