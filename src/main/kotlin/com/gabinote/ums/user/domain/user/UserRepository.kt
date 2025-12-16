package com.gabinote.ums.user.domain.user

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, ObjectId> {
    fun findByNickname(nickname: String): User?
    fun findByUid(uid: String): User?
    fun existsByUid(uid: String): Boolean
}