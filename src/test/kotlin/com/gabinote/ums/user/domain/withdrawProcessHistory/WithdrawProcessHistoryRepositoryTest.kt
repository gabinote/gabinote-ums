package com.gabinote.ums.user.domain.withdrawProcessHistory

import com.gabinote.ums.testSupport.testTemplate.RepositoryTestTemplate
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired

class WithdrawProcessHistoryRepositoryTest : RepositoryTestTemplate() {
    override val baseData = "base.json"
    override val baseDataDir = "/testsets/user/domain/withdrawProcessHistory"

    @Autowired
    private lateinit var withdrawProcessHistoryRepository: WithdrawProcessHistoryRepository

    init {
        describe("[User] WithdrawProcessHistoryRepositoryTest") {
            describe("WithdrawProcessHistoryRepositoryTest.save(신규)") {
                context("신규 WithdrawProcessHistory가 주어지면") {
                    testDataHelper.setData("$baseDataDir/save-before.json")
                    it("WithdrawProcessHistory를 저장한다") {
                        val newHistory = WithdrawProcessHistory(
                            uid = TestUuidSource.UUID_STRING.toString(),
                            requestId = ObjectId("507f1f77bcf86cd799439011"),
                            isPassed = true,
                            process = "APPLICATION_USER_DELETE"
                        )
                        withdrawProcessHistoryRepository.save(newHistory)
                        testDataHelper.assertData("$baseDataDir/save-after.json")
                    }
                }
            }
        }
    }
}

