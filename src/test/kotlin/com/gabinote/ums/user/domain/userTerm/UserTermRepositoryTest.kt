package com.gabinote.ums.user.domain.userTerm

import com.gabinote.ums.testSupport.testTemplate.RepositoryTestTemplate
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class UserTermRepositoryTest : RepositoryTestTemplate() {
    override val baseData = "base.json"
    override val baseDataDir = "/testsets/user/domain/userTerm"

    @Autowired
    private lateinit var userTermRepository: UserTermRepository

    init {
        describe("[UserTerm] UserTermRepositoryTest") {

            describe("UserTermRepositoryTest.findAllByUserId") {
                context("유효한 userId가 주어지면") {
                    useBaseData()
                    val validUserId = "user_ext_1001"
                    val pageable = PageRequest.of(0, 10)
                    it("해당 userId를 가진 UserTerm 목록을 반환한다") {
                        val res = userTermRepository.findAllByUserId(validUserId, pageable)

                        res.content shouldHaveSize 2
                        res.content.forEach { it.userId shouldBe validUserId }
                    }
                }

                context("유효하지 않은 userId가 주어지면") {
                    useBaseData()
                    val invalidUserId = "user_ext_9999"
                    val pageable = PageRequest.of(0, 10)
                    it("빈 페이지를 반환한다") {
                        val res = userTermRepository.findAllByUserId(invalidUserId, pageable)

                        res.content shouldHaveSize 0
                    }
                }
            }

            describe("UserTermRepositoryTest.findByUserIdAndTermCodeAndTermVersion") {
                context("유효한 userId, termCode, termVersion이 주어지면") {
                    useBaseData()
                    val validUserId = "user_ext_1001"
                    val validTermCode = "TERMS_OF_SERVICE"
                    val validTermVersion = "1.0.0"

                    it("해당 조건을 만족하는 UserTerm을 반환한다") {
                        val res = userTermRepository.findByUserIdAndTermCodeAndTermVersion(
                            validUserId,
                            validTermCode,
                            validTermVersion
                        )

                        res shouldNotBe null
                        res!!.userId shouldBe validUserId
                        res.termCode shouldBe validTermCode
                        res.termVersion shouldBe validTermVersion
                    }
                }

                context("존재하지 않는 termCode가 주어지면") {
                    useBaseData()
                    val validUserId = "user_ext_1001"
                    val invalidTermCode = "NONEXISTENT_TERM"
                    val validTermVersion = "1.0.0"

                    it("null을 반환한다") {
                        val res = userTermRepository.findByUserIdAndTermCodeAndTermVersion(
                            validUserId,
                            invalidTermCode,
                            validTermVersion
                        )

                        res shouldBe null
                    }
                }

                context("존재하지 않는 termVersion이 주어지면") {
                    useBaseData()
                    val validUserId = "user_ext_1001"
                    val validTermCode = "TERMS_OF_SERVICE"
                    val invalidTermVersion = "9.9.9"

                    it("null을 반환한다") {
                        val res = userTermRepository.findByUserIdAndTermCodeAndTermVersion(
                            validUserId,
                            validTermCode,
                            invalidTermVersion
                        )

                        res shouldBe null
                    }
                }
            }

            describe("UserTermRepositoryTest.save(신규)") {
                context("신규 UserTerm이 주어지면") {
                    testDataHelper.setData("$baseDataDir/save-before.json")
                    it("UserTerm을 저장한다") {
                        val newUserTerm = UserTerm(
                            userId = "user_ext_1001",
                            termCode = "MARKETING_CONSENT",
                            termVersion = "1.0.0",
                            accepted = true
                        )
                        
                        val savedUserTerm = userTermRepository.save(newUserTerm)
                        
                        savedUserTerm.id shouldNotBe null
                        testDataHelper.assertData("$baseDataDir/save-after.json")
                    }
                }
            }

            describe("UserTermRepositoryTest.save(수정)") {
                context("기존 UserTerm이 주어지면") {
                    testDataHelper.setData("$baseDataDir/update-before.json")
                    val userId = "user_ext_1001"
                    val existingUserTerm = userTermRepository.findByUserIdAndTermCodeAndTermVersion(
                        userId,
                        "TERMS_OF_SERVICE",
                        "1.0.0"
                    )!!

                    existingUserTerm.apply {
                        termVersion = "2.0.0"
                    }

                    it("UserTerm을 수정한다") {
                        val updatedUserTerm = userTermRepository.save(existingUserTerm)
                        
                        updatedUserTerm.termVersion shouldBe "2.0.0"
                        testDataHelper.assertData("$baseDataDir/update-after.json")
                    }
                }
            }

            describe("UserTermRepositoryTest.findAll") {
                context("여러 UserTerm이 저장되어 있으면") {
                    useBaseData()
                    it("모든 UserTerm을 반환한다") {
                        val allUserTerms = userTermRepository.findAll()

                        allUserTerms shouldHaveSize 3
                    }
                }
            }

            describe("UserTermRepositoryTest.delete") {
                context("존재하는 UserTerm이 주어지면") {
                    useBaseData()
                    val userId = "user_ext_1001"
                    val userTerm = userTermRepository.findByUserIdAndTermCodeAndTermVersion(
                        userId,
                        "TERMS_OF_SERVICE",
                        "1.0.0"
                    )!!

                    it("UserTerm을 삭제한다") {
                        userTermRepository.delete(userTerm)

                        val deletedUserTerm = userTermRepository.findByUserIdAndTermCodeAndTermVersion(
                            userId,
                            "TERMS_OF_SERVICE",
                            "1.0.0"
                        )

                        deletedUserTerm shouldBe null
                    }
                }
            }
        }
    }
}

