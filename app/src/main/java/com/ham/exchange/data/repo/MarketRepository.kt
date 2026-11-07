package com.ham.exchange.data.repo

import com.ham.exchange.data.api.NetworkModule
import com.ham.exchange.data.api.toCoin
import com.ham.exchange.data.model.Coin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarketRepository {
    private val _coins = MutableStateFlow<List<Coin>>(emptyList())
    val coins: StateFlow<List<Coin>> = _coins

    suspend fun refresh(topN: Int = 300) {
        val result = mutableListOf<Coin>()
        var page = 1
        while (result.size < topN) {
            val batch = NetworkModule.coinGecko.markets(perPage = 100, page = page)
            if (batch.isEmpty()) break
            result += batch.map { it.toCoin() }
            page++
        }
        _coins.value = result.take(topN)
    }
}
