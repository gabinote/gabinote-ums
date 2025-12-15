package com.gabinote.ums.testSupport.testUtil.time

import com.gabinote.ums.common.util.time.TimeProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestTimeConfig {

    @Bean
    fun timeProvider(): TimeProvider {
        return TestTimeProvider()
    }
}