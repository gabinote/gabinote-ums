package com.gabinote.ums.policy.domain.policy

enum class PolicyKey(val key: String, val description: String) {
    USER_REGISTER_BASE_GROUP(
        key = "user_register_base_group",
        description = "사용자 가입 시 기본으로 할당되는 그룹",
    ),
    USER_ENABLED_REGISTER(
        key = "user_enabled_register",
        description = "사용자 가입 가능 여부",
    )
}