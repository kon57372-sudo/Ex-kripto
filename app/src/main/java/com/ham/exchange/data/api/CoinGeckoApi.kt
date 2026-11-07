package com.ham.exchange.data.api

import com.ham.exchange.data.model.Coin
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("coins/markets")
    suspend fun markets(
        @Query("vs_currency") vs: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Query("price_change_percentage") pcp: String = "1h,24h,7d"
    ): List<CoinGeckoCoin>
}

data class CoinGeckoCoin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String?,
    val current_price: Double,
    val market_cap: Double,
    val total_volume: Double,
    val price_change_percentage_1h_in_currency: Double?,
    val price_change_percentage_24h: Double?,
    val price_change_percentage_7d_in_currency: Double?,
    val market_cap_rank: Int?,
)
