package com.ham.exchange.data.api

import com.ham.exchange.data.model.Coin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object NetworkModule {
    val okHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val coinGecko: CoinGeckoApi = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CoinGeckoApi::class.java)
}

fun CoinGeckoCoin.toCoin() = Coin(
    id = id, symbol = symbol.uppercase(), name = name, image = image,
    currentPrice = current_price, marketCap = market_cap, totalVolume = total_volume,
    priceChangePercentage1hInCurrency = price_change_percentage_1h_in_currency,
    priceChangePercentage24h = price_change_percentage_24h,
    priceChangePercentage7dInCurrency = price_change_percentage_7d_in_currency,
    marketCapRank = market_cap_rank
)
