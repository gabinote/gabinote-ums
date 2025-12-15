package com.gabinote.ums.common.util.collection

object CollectionHelper {
    fun Collection<*>.firstOrEmptyString(): String {
        return this.firstOrNull()?.toString() ?: ""
    }
}