package com.gabinote.ums.common.config.cache

enum class CacheDefine(
    val cacheName: String,
    val expireAfterWriteMinutes: Long = 10L,
    val maximumSize: Long = 1000L,
) {
    POLICY_CACHE(
        cacheName = "policy",
        expireAfterWriteMinutes = 30L,
        maximumSize = 500L,
    ),
}