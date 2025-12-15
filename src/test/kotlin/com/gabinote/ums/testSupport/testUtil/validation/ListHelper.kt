package com.gabinote.ums.testSupport.testUtil.validation

object ListHelper {
    fun <T> List<T>.isSameElements(list1: List<T>): Boolean {
        if (this.size != list1.size) return false
        val map = mutableMapOf<T, Int>()
        for (item in this) {
            map[item] = map.getOrDefault(item, 0) + 1
        }
        for (item in list1) {
            val count = map.getOrDefault(item, 0)
            if (count == 0) return false
            map[item] = count - 1
        }
        return true
    }
}