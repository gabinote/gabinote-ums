package com.gabinote.ums.user.event.userWithdraw

enum class WithdrawProcess(val value: String){
    APPLICATION_USER_DELETE("APPLICATION_USER_DELETE"),
    KEYCLOAK_USER_DELETE("KEYCLOAK_USER_DELETE"),
    NOTE_DELETE("NOTE_DELETE"),
    IMAGE_DELETE("IMAGE_DELETE"),
}