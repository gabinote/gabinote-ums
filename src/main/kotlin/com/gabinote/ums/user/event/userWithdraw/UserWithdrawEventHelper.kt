package com.gabinote.ums.user.event.userWithdraw

object UserWithdrawEventHelper{
    const val USER_WITHDRAW_EVENT_TYPE = "ums.user.withdraw.requested"
    const val USER_WITHDRAW_EVENT_TYPE_DLQ = "ums.user.withdraw.requested.DLQ"
    const val USER_WITHDRAW_USER_DELETE_GROUP = "ums-user-withdraw-handler-user-delete"
}