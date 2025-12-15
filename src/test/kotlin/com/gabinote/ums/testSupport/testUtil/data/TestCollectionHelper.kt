package com.gabinote.ums.testSupport.testUtil.data

object TestCollectionHelper {
    fun generateRandomStringSet(maxLength: Int, count: Int): Set<String> {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val random = java.util.Random()

        return buildSet {
            while (size < count) {
                // 1부터 maxLength 사이의 랜덤 길이 결정
                val length = random.nextInt(maxLength) + 1

                // 랜덤 길이의 문자열 생성
                val randomString = (1..length)
                    .map { charset[random.nextInt(charset.size)] }
                    .joinToString("")

                // 세트에 추가 (중복은 자동으로 무시됨)
                add(randomString)
            }
        }
    }
}