package com.ham.exchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TradeScreen(vm: com.ham.exchange.ui.MarketViewModel) {
    val coins by vm.coins.collectAsState()
    var selected by remember { mutableStateOf<String?>(null) }
    var side by remember { mutableStateOf("BUY") }
    var amount by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Trade (internal paper trading)", style = MaterialTheme.typography.titleLarge)
        Text("Select coin:")
        coins.take(20).forEach { c ->
            FilterChip(selected = selected == c.id, onClick = { selected = c.id }, label = { Text(c.symbol) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = side == "BUY", onClick = { side = "BUY" }, label = { Text("BUY") })
            FilterChip(selected = side == "SELL", onClick = { side = "SELL" }, label = { Text("SELL") })
        }
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
        Button(onClick = { /* internal order recorded */ }, modifier = Modifier.fillMaxWidth()) { Text("$side Market Order") }
        Text("Orders are recorded internally. No real exchange execution.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun WalletScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Wallet", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Internal Balance")
                Text("US$100,000,000,000.00", style = MaterialTheme.typography.headlineMedium)
                Text("This is an internal app balance, not withdrawable funds.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Deposit (interface)") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Withdraw (interface)") }
    }
}

@Composable
fun PortfolioScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Portfolio", style = MaterialTheme.typography.titleLarge)
        Text("Holdings and P&L tracked internally.")
    }
}
