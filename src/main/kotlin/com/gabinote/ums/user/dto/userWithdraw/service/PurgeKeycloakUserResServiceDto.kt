package com.gabinote.ums.user.dto.userWithdraw.service

data class PurgeKeycloakUserResServiceDto(
    var success: Long = 0L,
    var failed: Long = 0L,
    var total: Long = 0L

) {

    fun addSuccess(cnt: Int) {
        this.success += cnt
    }

    fun addFailed(cnt: Int) {
        this.failed += cnt
    }

    fun plushSuccess() {
        this.success += 1
    }

    fun plushFailed() {
        this.failed += 1
    }
}