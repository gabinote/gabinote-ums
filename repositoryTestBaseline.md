```kotlin
package com.gabinote.coffeenote.note.domain.note

import com.gabinote.coffeenote.field.domain.attribute.Attribute
import com.gabinote.coffeenote.testSupport.testTemplate.RepositoryTestTemplate
import com.gabinote.coffeenote.testSupport.testUtil.data.note.NoteHashTestDataHelper
import com.gabinote.coffeenote.testSupport.testUtil.page.TestPageableUtil.createPageable
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class NoteRepositoryTest : RepositoryTestTemplate() {

    override val baseData = "base.json"
    override val baseDataDir = "/testsets/note/domain/note"

    @Autowired
    private lateinit var noteRepository: NoteRepository

    init {
        describe("[Note] NoteRepositoryTest") {
            describe("NoteRepositoryTest.findByExternalId") {
                context("유효한 externalId가 주어지면") {
                    useBaseData()
                    val validExternalId = "a1b2c3d4-e5f6-4789-0123-4567890abcde"
                    it("해당 externalId를 가진 Note를 반환한다") {
                        val res = noteRepository.findByExternalId(validExternalId)

                        res!!.externalId shouldBe validExternalId
                    }
                }

                context("유효하지 않은 externalId가 주어지면") {
                    useBaseData()
                    val invalidExternalId = "00000000-0000-0000-0000-000000000000"
                    it("null을 반환한다") {
                        val res = noteRepository.findByExternalId(invalidExternalId)

                        res shouldBe null
                    }
                }
            }

            describe("NoteRepositoryTest.findAllByOwner") {
                context("유효한 owner 가 주어지면") {
                    useBaseData()
                    val validOwnerId = "user_g7h8_3790"
                    val testPageable = createPageable()
                    it("해당 ownerId를 가진 Note 리스트를 반환한다") {
                        val res = noteRepository.findAllByOwner(validOwnerId, testPageable)
                        res.forEach {
                            it.owner shouldBe validOwnerId
                        }
                    }
                }

                context("노트가 없는 owner 가 주어지면") {
                    useBaseData()
                    val ownerIdWithNoNotes = "owner-0000"
                    val testPageable = createPageable()
                    it("빈 리스트를 반환한다") {
                        val res = noteRepository.findAllByOwner(ownerIdWithNoNotes, testPageable)
                        res.content.size shouldBe 0
                    }
                }
            }

            describe("NoteRepositoryTest.countByOwner") {
                context("유효한 owner 가 주어지면") {
                    useBaseData()
                    val validOwnerId = "user_g7h8_3790"
                    it("해당 ownerId를 가진 Note 개수를 반환한다") {
                        val res = noteRepository.countByOwner(validOwnerId)
                        res shouldBe 1L
                    }
                }

                context("노트가 없는 owner 가 주어지면") {
                    useBaseData()
                    val ownerIdWithNoNotes = "owner-0000"
                    it("0을 반환한다") {
                        val res = noteRepository.countByOwner(ownerIdWithNoNotes)
                        res shouldBe 0L
                    }
                }
            }

            describe("NoteRepositoryTest.deleteAllByOwner") {
                context("유효한 owner 가 주어지면") {
                    testDataHelper.setData("$baseDataDir/delete-before.json")
                    val validOwnerId = "target"
                    it("해당 ownerId를 가진 Note 들을 삭제한다") {
                        noteRepository.deleteAllByOwner(validOwnerId)
                        testDataHelper.assertData("$baseDataDir/delete-after.json")
                    }
                }

                context("노트가 없는 owner 가 주어지면") {
                    testDataHelper.setData("$baseDataDir/delete-before.json")
                    val ownerIdWithNoNotes = "owner-0000"
                    it("아무일도 일어나지 않는다") {
                        noteRepository.deleteAllByOwner(ownerIdWithNoNotes)
                        testDataHelper.assertData("$baseDataDir/delete-after-fail.json")
                    }
                }
            }

            describe("NoteRepositoryTest.save(신규)") {
                context("신규 Note가 주어지면") {
                    testDataHelper.setData("$baseDataDir/save-before.json")
                    it("Note를 저장한다") {
                        val newNote = Note(
                            title = "콜드브루 시음 기록",
                            thumbnail = "https://images.example.com/thumbnails/cold_brew_sample.png",
                            createdDate = LocalDateTime.parse("2024-07-27T08:10:55.000"),
                            modifiedDate = LocalDateTime.parse("2024-07-27T08:10:55.000"),
                            fields = listOf(
                                NoteField(
                                    id = "field_005",
                                    name = "산미",
                                    icon = "lemon",
                                    type = "RATING",
                                    attributes = setOf(Attribute(key = "max", value = setOf("5"))),
                                    order = 1,
                                    isDisplay = true,
                                    values = setOf("4")
                                ),
                                NoteField(
                                    id = "field_006",
                                    name = "플레이버 노트",
                                    icon = "flavor",
                                    type = "MULTI_SELECT",
                                    attributes = setOf(
                                        Attribute(
                                            key = "options",
                                            value = setOf("초콜릿", "견과류", "베리", "꽃")
                                        )
                                    ),
                                    order = 2,
                                    isDisplay = true,
                                    values = setOf("초콜릿", "견과류")
                                )
                            ),
                            displayFields = listOf(
                                NoteDisplayField(name = "산미", icon = "lemon", values = setOf("4"), order = 1),
                                NoteDisplayField(
                                    name = "플레이버 노트",
                                    icon = "flavor",
                                    values = setOf("초콜릿", "견과류"),
                                    order = 2
                                )
                            ),
                            isOpen = true,
                            owner = "user_gamma_9012",
                            hash = NoteHashTestDataHelper.TEST_HASH
                        )
                        noteRepository.save(newNote)
                        testDataHelper.assertData("$baseDataDir/save-after.json")
                    }
                }
            }

            describe("NoteRepositoryTest.save(수정)") {
                context("기존 Note가 주어지면") {
                    testDataHelper.setData("$baseDataDir/update-before.json")
                    val existingNote = noteRepository.findByExternalId("a1b2c3d4-e5f6-4789-0123-4567890abcde")!!
                    existingNote.apply {
                        title = "콜드브루 시음 기록"
                        thumbnail = "https://images.example.com/thumbnails/cold_brew_sample.png"
                        fields = listOf(
                            NoteField(
                                id = "field_005",
                                name = "산미",
                                icon = "lemon",
                                type = "RATING",
                                attributes = setOf(Attribute(key = "max", value = setOf("5"))),
                                order = 1,
                                isDisplay = true,
                                values = setOf("4")
                            ),
                            NoteField(
                                id = "field_006",
                                name = "플레이버 노트",
                                icon = "flavor",
                                type = "MULTI_SELECT",
                                attributes = setOf(
                                    Attribute(
                                        key = "options",
                                        value = setOf("초콜릿", "견과류", "베리", "꽃")
                                    )
                                ),
                                order = 2,
                                isDisplay = true,
                                values = setOf("초콜릿", "견과류")
                            )
                        )
                        displayFields = listOf(
                            NoteDisplayField(name = "산미", icon = "lemon", values = setOf("4"), order = 1),
                            NoteDisplayField(
                                name = "플레이버 노트",
                                icon = "flavor",
                                values = setOf("초콜릿", "견과류"),
                                order = 2
                            )
                        )
                        isOpen = true
                        hash = "updated-hash"
                    }
                    it("Note를 수정한다") {
                        noteRepository.save(existingNote)
                        testDataHelper.assertData("$baseDataDir/update-after.json")
                    }
                }
            }
        }
    }
}
```