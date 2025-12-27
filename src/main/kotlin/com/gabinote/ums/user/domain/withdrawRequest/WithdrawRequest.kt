package com.gabinote.ums.user.domain.withdrawRequest

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "withdraw_requests")
data class WithdrawRequest(
    @Id
    var id: ObjectId? = null,

    var uid: String,
    var email: String,
    @CreatedDate
    var createdDate: LocalDateTime? = null,

    // 실제 keycloak 유저 데이터 삭제까지 처리되었는지 나타내는 상태값임.
    // 해당 상태가 이제 삭제 완료되면 동일 계정으로 재가입이 가능해짐.
    var purgeStatus: String,
    var purgeTryCnt: Long = 0
) {
    fun updateStatus(withdrawPurgeStatus: WithdrawPurgeStatus) {
        this.purgeStatus = withdrawPurgeStatus.value
    }

    fun addTryCnt() {
        this.purgeTryCnt += 1
    }

    fun updateTryCnt(tryCnt: Long) {
        this.purgeTryCnt = tryCnt
    }
}
