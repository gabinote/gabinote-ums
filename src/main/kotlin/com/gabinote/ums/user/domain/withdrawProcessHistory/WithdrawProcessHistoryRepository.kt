package com.gabinote.ums.user.domain.withdrawProcessHistory

import com.gabinote.ums.user.domain.withdrawRequest.WithdrawRequest
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WithdrawProcessHistoryRepository : MongoRepository<WithdrawProcessHistory, ObjectId> {
}