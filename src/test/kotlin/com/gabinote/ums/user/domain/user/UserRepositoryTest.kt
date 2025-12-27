package com.gabinote.ums.user.domain.user

import com.gabinote.ums.testSupport.testTemplate.RepositoryTestTemplate
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired

class UserRepositoryTest : RepositoryTestTemplate() {
    override val baseData = "base.json"
    override val baseDataDir = "/testsets/user/domain/user"

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        describe("[User] UserRepositoryTest") {
            describe("UserRepositoryTest.findByUid") {
                context("유효한 uid가 주어지면") {
                    useBaseData()
                    val validExternalId = "user_ext_1001"
                    it("해당 externalId를 가진 User를 반환한다") {
                        val res = userRepository.findByUid(validExternalId)

                        res!!.uid shouldBe validExternalId
                    }
                }
                context("유효하지 않은 uid가 주어지면") {
                    useBaseData()
                    val invalidExternalId = "user_ext_0000"
                    it("null을 반환한다") {
                        val res = userRepository.findByUid(invalidExternalId)

                        res shouldBe null
                    }
                }
            }

            describe("UserRepositoryTest.findByNickname") {
                context("유효한 nickname이 주어지면") {
                    useBaseData()
                    val validNickname = "DevMaster"
                    it("해당 nickname을 가진 User를 반환한다") {
                        val res = userRepository.findByNickname(validNickname)

                        res!!.nickname shouldBe validNickname
                    }
                }

                context("유효하지 않은 nickname이 주어지면") {
                    useBaseData()
                    val invalidNickname = "NonExistentUser"
                    it("null을 반환한다") {
                        val res = userRepository.findByNickname(invalidNickname)

                        res shouldBe null
                    }
                }
            }

            describe("UserRepositoryTest.save(신규)") {
                context("신규 User가 주어지면") {
                    testDataHelper.setData("$baseDataDir/save-before.json")
                    it("User를 저장한다") {
                        val newUser = User(
                            uid = TestUuidSource.UUID_STRING.toString(),
                            nickname = "NewUser",
                            profileImg = "https://cdn.gabinote.com/profiles/new_user.png",
                            isOpenProfile = true,
                            isMarketingEmailAgreed = false,
                            isMarketingPushAgreed = false,
                            isNightPushAgreed = false
                        )
                        userRepository.save(newUser)
                        testDataHelper.assertData("$baseDataDir/save-after.json")
                    }
                }
            }

            describe("UserRepositoryTest.save(수정)") {
                context("기존 User가 주어지면") {
                    testDataHelper.setData("$baseDataDir/update-before.json")
                    val existingUser = userRepository.findByUid("user_ext_1001")!!
                    existingUser.apply {
                        nickname = "UpdatedNickname"
                        profileImg = "https://cdn.gabinote.com/profiles/updated.png"
                    }
                    it("User를 수정한다") {
                        userRepository.save(existingUser)
                        testDataHelper.assertData("$baseDataDir/update-after.json")
                    }
                }
            }
        }
    }
}