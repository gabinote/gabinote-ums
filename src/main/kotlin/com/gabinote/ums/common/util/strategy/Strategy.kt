package com.gabinote.ums.common.util.strategy

interface Strategy<T : Enum<T>> {
    val type: T
}