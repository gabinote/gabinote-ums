package com.gabinote.ums.policy.domain.policy

enum class PolicyKey(val key: String, val description: String) {
    USER_REGISTER_BASE_ROLE(
        key = "user.register.base_role",
        description = "사용자 가입 시 기본으로 할당되는 Role",
    ),
    USER_ENABLED_REGISTER(
        key = "user.register.enabled",
        description = "사용자 가입 가능 여부",
    ),
    USER_PURGE_CUTOFF_DAYS(
        key = "user.withdraw.purge.cutoff_days",
        description = "사용자 완전 삭제 처리까지의 유예 기간 (일)",
    ),
}