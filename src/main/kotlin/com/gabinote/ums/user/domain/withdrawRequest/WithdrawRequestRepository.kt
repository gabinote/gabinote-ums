package com.gabinote.ums.user.domain.withdrawRequest

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface WithdrawRequestRepository : MongoRepository<WithdrawRequest, ObjectId> {
    fun findByUid(uid: String): WithdrawRequest?
    fun findAllByPurgeStatusAndCreatedDateLessThanEqual(purgeStatus:String, cutoffCreatedDate: LocalDateTime, pageable: Pageable): List<WithdrawRequest>
    fun countAllByPurgeStatusAndCreatedDateLessThanEqual(purgeStatus:String, cutoffCreatedDate: LocalDateTime): Long
}