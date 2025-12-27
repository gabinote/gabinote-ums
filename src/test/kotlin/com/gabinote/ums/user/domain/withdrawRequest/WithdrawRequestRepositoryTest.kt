package com.gabinote.ums.user.domain.withdrawRequest

import com.gabinote.ums.testSupport.testTemplate.RepositoryTestTemplate
import com.gabinote.ums.testSupport.testUtil.page.TestPageableUtil.createPageable
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class WithdrawRequestRepositoryTest : RepositoryTestTemplate() {
    override val baseData = "base.json"
    override val baseDataDir = "/testsets/user/domain/withdrawRequest"

    @Autowired
    private lateinit var withdrawRequestRepository: WithdrawRequestRepository

    init {
        describe("[User] WithdrawRequestRepositoryTest") {
            describe("WithdrawRequestRepositoryTest.findByUid") {
                context("유효한 uid가 주어지면") {
                    useBaseData()
                    val validUid = "user_ext_1001"
                    it("해당 uid를 가진 WithdrawRequest를 반환한다") {
                        val res = withdrawRequestRepository.findByUid(validUid)

                        res!!.uid shouldBe validUid
                    }
                }

                context("유효하지 않은 uid가 주어지면") {
                    useBaseData()
                    val invalidUid = "non_existing_user"
                    it("null을 반환한다") {
                        val res = withdrawRequestRepository.findByUid(invalidUid)

                        res shouldBe null
                    }
                }
            }

            describe("WithdrawRequestRepositoryTest.findAllByPurgeStatusAndCreatedDateLessThanEqual") {
                context("유효한 purgeStatus와 cutoffDate가 주어지면") {
                    useBaseData()
                    val purgeStatus = "PENDING"
                    val cutoffDate = LocalDateTime.parse("2024-01-06T00:00:00")
                    val pageable = createPageable()
                    it("조건에 맞는 WithdrawRequest 리스트를 반환한다") {
                        val res = withdrawRequestRepository.findAllByPurgeStatusAndCreatedDateLessThanEqual(
                            purgeStatus, cutoffDate, pageable
                        )

                        res.size shouldBe 2
                        res.forEach {
                            it.purgeStatus shouldBe purgeStatus
                        }
                    }
                }

                context("조건에 맞는 데이터가 없으면") {
                    useBaseData()
                    val purgeStatus = "PENDING"
                    val cutoffDate = LocalDateTime.parse("2023-01-01T00:00:00")
                    val pageable = createPageable()
                    it("빈 리스트를 반환한다") {
                        val res = withdrawRequestRepository.findAllByPurgeStatusAndCreatedDateLessThanEqual(
                            purgeStatus, cutoffDate, pageable
                        )

                        res.size shouldBe 0
                    }
                }
            }

            describe("WithdrawRequestRepositoryTest.countAllByPurgeStatusAndCreatedDateLessThanEqual") {
                context("유효한 purgeStatus와 cutoffDate가 주어지면") {
                    useBaseData()
                    val purgeStatus = "PENDING"
                    val cutoffDate = LocalDateTime.parse("2024-01-06T00:00:00")
                    it("조건에 맞는 WithdrawRequest 개수를 반환한다") {
                        val res = withdrawRequestRepository.countAllByPurgeStatusAndCreatedDateLessThanEqual(
                            purgeStatus, cutoffDate
                        )

                        res shouldBe 2L
                    }
                }

                context("조건에 맞는 데이터가 없으면") {
                    useBaseData()
                    val purgeStatus = "PENDING"
                    val cutoffDate = LocalDateTime.parse("2023-01-01T00:00:00")
                    it("0을 반환한다") {
                        val res = withdrawRequestRepository.countAllByPurgeStatusAndCreatedDateLessThanEqual(
                            purgeStatus, cutoffDate
                        )

                        res shouldBe 0L
                    }
                }
            }

            describe("WithdrawRequestRepositoryTest.save(신규)") {
                context("신규 WithdrawRequest가 주어지면") {
                    testDataHelper.setData("$baseDataDir/save-before.json")
                    it("WithdrawRequest를 저장한다") {
                        val newRequest = WithdrawRequest(
                            uid = TestUuidSource.UUID_STRING.toString(),
                            email = "newuser@example.com",
                            purgeStatus = WithdrawPurgeStatus.PENDING.value,
                            purgeTryCnt = 0
                        )
                        withdrawRequestRepository.save(newRequest)
                        testDataHelper.assertData("$baseDataDir/save-after.json")
                    }
                }
            }

            describe("WithdrawRequestRepositoryTest.save(수정)") {
                context("기존 WithdrawRequest가 주어지면") {
                    testDataHelper.setData("$baseDataDir/update-before.json")

                    val existingRequest = withdrawRequestRepository.findByUid("user_ext_1001")!!
                    it("WithdrawRequest를 수정한다") {
                        existingRequest.apply {
                            updateStatus(WithdrawPurgeStatus.COMPLETED)
                            updateTryCnt(3)
                        }
                        withdrawRequestRepository.save(existingRequest)
                        testDataHelper.assertData("$baseDataDir/update-after.json")
                    }
                }
            }

            describe("WithdrawRequestRepositoryTest.delete") {
                context("기존 WithdrawRequest가 주어지면") {
                    testDataHelper.setData("$baseDataDir/delete-before.json")
                    it("WithdrawRequest를 삭제한다") {
                        val targetRequest = withdrawRequestRepository.findByUid("target_user")!!
                        withdrawRequestRepository.delete(targetRequest)
                        testDataHelper.assertData("$baseDataDir/delete-after.json")
                    }
                }
            }
        }
    }
}

