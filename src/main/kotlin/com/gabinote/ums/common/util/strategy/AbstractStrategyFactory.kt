package com.gabinote.ums.common.util.strategy

abstract class AbstractStrategyFactory<E : Enum<E>, S : Strategy<E>>(
    private val strategies: List<S>,
) {
    private var strategyMap: Map<E, S> = strategies.associateBy { it.type }


    fun getStrategy(key: E): S {
        return strategyMap[key] ?: throw IllegalArgumentException("Invalid strategy key: $key")
    }

}