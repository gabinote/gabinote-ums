package com.gabinote.ums.common.util.exception.service

class ForbiddenByPolicy(
    reason: String,
) : ServiceException() {

    override val errorMessage: String = "Action forbidden by policy: $reason"
    override val logMessage: String = errorMessage

}