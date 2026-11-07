package com.ham.exchange.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ham.exchange.data.repo.MarketRepository
import com.ham.exchange.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    val repo = remember { MarketRepository() }
    val vm: MarketViewModel = viewModel { MarketViewModel(repo) }
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Markets", "Trade", "Wallet", "Portfolio")
    MaterialTheme(colorScheme = darkColorScheme()) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = {
                        Icon(when (i) { 0 -> Icons.Filled.ShowChart; 1 -> Icons.Filled.SwapHoriz; 2 -> Icons.Filled.AccountBalanceWallet; else -> Icons.Filled.PieChart }, null)
                    }, label = { Text(t) })
                }
            }
        }
    ) { p ->
        Box(Modifier.padding(p)) {
            when (tab) {
                0 -> MarketsScreen(vm)
                1 -> TradeScreen(vm)
                2 -> WalletScreen()
                3 -> PortfolioScreen()
            }
        }
    }
    }
}
