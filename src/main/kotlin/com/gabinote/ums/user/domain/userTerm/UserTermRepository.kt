package com.gabinote.ums.user.domain.userTerm

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserTermRepository : MongoRepository<UserTerm, ObjectId> {

    fun findAllByUserId(userId: String,pageable: Pageable): Page<UserTerm>
    fun findByUserIdAndTermCodeAndTermVersion(userId: String, termCode: String, termVersion: String): UserTerm?


}