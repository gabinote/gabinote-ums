package com.gabinote.ums.mail.enums

enum class MailType(
    val value: String
){
    ADMIN_ALERT("ADMIN_ALERT"),
    ALL_MARKET_ALERT("ALL_MARKET_ALERT"),
    ALL_ALERT("ALL_ALERT"),
    PER_USER_ALERT("PER_USER_ALERT"),
}