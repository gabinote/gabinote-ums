package com.gabinote.ums.outbox.domain

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OutBoxRepository : MongoRepository<OutBox, ObjectId> {
}