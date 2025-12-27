package com.gabinote.ums.common.util.context

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class UserContext(
    var isAuthorized: Boolean = false,
    private var _uid: String? = null,
    var roles: List<String> = emptyList()
) {
    var uid: String
        get() = _uid ?: throw UserContextNotFound()
        set(value) {
            _uid = value
        }

    fun isLoggedIn(): Boolean = _uid != null

    fun uidWithUUID(): java.util.UUID {
        return UUID.fromString(uid)
    }

    fun setContext(
        uid: String,
        roles: List<String>
    ) {
        this.isAuthorized = true
        this._uid = uid
        this.roles = roles
    }
}