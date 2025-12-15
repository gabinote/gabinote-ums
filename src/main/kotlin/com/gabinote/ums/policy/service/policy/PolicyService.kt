package com.gabinote.ums.policy.service.policy

import com.gabinote.ums.policy.domain.policy.Policy
import com.gabinote.ums.policy.domain.policy.PolicyKey
import com.gabinote.ums.policy.domain.policy.PolicyRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PolicyService(
    private val policyRepository: PolicyRepository,
) {

    fun fetchByKey(key: String): Policy {
        // 해당 정책이 없는건 서버 설정 오류이므로 예외 발생
        return policyRepository.findByKey(key)
            ?: throw IllegalArgumentException("Policy with key $key not found")
    }

    @Cacheable(cacheNames = ["policy"], key = "#key")
    fun getByKey(key: String): String {
        return fetchByKey(key).value
    }

    @Cacheable(cacheNames = ["policy"], key = "#key.key")
    fun getByKey(key: PolicyKey): String {
        return fetchByKey(key.key).value
    }
}