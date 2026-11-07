package com.ham.exchange.data.model

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String?,
    val currentPrice: Double,
    val marketCap: Double,
    val totalVolume: Double,
    val priceChangePercentage1hInCurrency: Double?,
    val priceChangePercentage24h: Double?,
    val priceChangePercentage7dInCurrency: Double?,
    val marketCapRank: Int?,
)

data class Holding(val coinId: String, val symbol: String, val amount: Double, val avgBuyPrice: Double)
data class Order(val id: Long = 0, val coinId: String, val symbol: String, val side: String, val type: String, val price: Double, val amount: Double, val status: String, val timestamp: Long)
data class WatchlistItem(val coinId: String)
