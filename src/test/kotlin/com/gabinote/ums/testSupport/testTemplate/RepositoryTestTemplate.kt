package com.gabinote.ums.testSupport.testTemplate


import com.gabinote.ums.testSupport.testConfig.db.UseTestDatabase
import com.gabinote.ums.testSupport.testUtil.database.TestDataHelper
import com.gabinote.ums.testSupport.testUtil.time.TestTimeConfig
import com.gabinote.ums.testSupport.testUtil.uuid.TestUuidSource
import com.gabinote.ums.common.config.MongodbConfig
import com.gabinote.ums.common.util.auditor.extId.ExternalIdListener
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import


@ExtendWith(MockKExtension::class)
@Import(
    TestDataHelper::class,
    TestUuidSource::class,
    ExternalIdListener::class,
    TestTimeConfig::class,
    MongodbConfig::class,
)
@DataMongoTest
@UseTestDatabase
abstract class RepositoryTestTemplate : DescribeSpec() {

    @Autowired
    lateinit var testDataHelper: TestDataHelper

    val baseDataDir = "/testsets/note/domain"
    val baseData = "base.json"
    fun useBaseData() {
        testDataHelper.setData("$baseDataDir/$baseData")
    }
}