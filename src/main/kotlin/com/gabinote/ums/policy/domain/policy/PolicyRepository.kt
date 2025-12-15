package com.gabinote.ums.policy.domain.policy

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PolicyRepository : MongoRepository<Policy, ObjectId> {
    fun findByKey(key: String): Policy?
}