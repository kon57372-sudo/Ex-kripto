package com.ham.exchange.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ham.exchange.data.model.Coin
import com.ham.exchange.data.repo.MarketRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketViewModel(private val repo: MarketRepository) : ViewModel() {
    val coins: StateFlow<List<Coin>> = repo.coins
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    init { refresh() }
    fun refresh() = viewModelScope.launch {
        _loading.value = true
        try { repo.refresh(300) } finally { _loading.value = false }
    }
}
