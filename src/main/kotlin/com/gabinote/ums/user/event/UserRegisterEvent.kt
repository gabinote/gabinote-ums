package com.gabinote.ums.user.event

data class UserRegisterEvent(
    val uid: String,
    val retryCount: Int = 0,
)