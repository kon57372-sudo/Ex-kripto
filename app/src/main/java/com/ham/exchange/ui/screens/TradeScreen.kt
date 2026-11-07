package com.ham.exchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ham.exchange.data.db.HamDatabase
import com.ham.exchange.data.db.HoldingEntity
import com.ham.exchange.data.db.OrderEntity
import com.ham.exchange.ui.MarketViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun TradeScreen(vm: MarketViewModel, db: HamDatabase) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ham", android.content.Context.MODE_PRIVATE) }
    val coinsState = vm.coins.collectAsState()
    val coins = coinsState.value
    var selected by remember { mutableStateOf<String?>(null) }
    var side by remember { mutableStateOf("BUY") }
    var amount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Trade", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            coins.take(8).forEach { c ->
                FilterChip(selected = selected == c.id, onClick = { selected = c.id }, label = { Text(c.symbol) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = side == "BUY", onClick = { side = "BUY" }, label = { Text("BUY") })
            FilterChip(selected = side == "SELL", onClick = { side = "SELL" }, label = { Text("SELL") })
        }
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (coins)") })
        Button(onClick = {
            val coin = coins.firstOrNull { it.id == selected } ?: return@Button
            val amt = amount.toDoubleOrNull() ?: return@Button
            scope.launch {
                val price = coin.currentPrice
                val cost = price * amt
                val balance = prefs.getFloat("balance", 100000000000f).toDouble()
                if (side == "BUY") {
                    if (balance < cost) return@launch
                    prefs.edit().putFloat("balance", (balance - cost).toFloat()).apply()
                    val existingList = db.holdings().all().firstOrNull() ?: emptyList()
                    val existing = existingList.firstOrNull { it.coinId == coin.id }
                    val newAmount = (existing?.amount ?: 0.0) + amt
                    db.holdings().upsert(HoldingEntity(coin.id, coin.symbol, newAmount, price))
                } else {
                    val existingList = db.holdings().all().firstOrNull() ?: emptyList()
                    val existing = existingList.firstOrNull { it.coinId == coin.id } ?: return@launch
                    if (existing.amount < amt) return@launch
                    val newAmount = existing.amount - amt
                    if (newAmount <= 0.0) db.holdings().delete(coin.id) else db.holdings().upsert(HoldingEntity(coin.id, coin.symbol, newAmount, existing.avgBuyPrice))
                    prefs.edit().putFloat("balance", (balance + cost).toFloat()).apply()
                }
                db.orders().insert(OrderEntity(coinId = coin.id, symbol = coin.symbol, side = side, type = "MARKET", price = price, amount = amt, status = "FILLED", timestamp = System.currentTimeMillis()))
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("$side Market Order") }
    }
}

@Composable
fun WalletScreen(db: HamDatabase) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ham", android.content.Context.MODE_PRIVATE) }
    val balance = prefs.getFloat("balance", 100000000000f)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Wallet", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Internal Balance")
                Text("US$${"%.2f".format(balance)}", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
fun PortfolioScreen(db: HamDatabase) {
    val holdingsState = db.holdings().all().collectAsState(initial = emptyList())
    val ordersState = db.orders().all().collectAsState(initial = emptyList())
    val holdings = holdingsState.value
    val orders = ordersState.value
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Portfolio", style = MaterialTheme.typography.titleLarge)
        Text("Holdings: ${holdings.size}")
        holdings.forEach { h -> Text("${h.symbol}: ${h.amount} @ ${h.avgBuyPrice}") }
        Spacer(Modifier.height(12.dp))
        Text("Recent Orders")
        orders.take(20).forEach { o -> Text("${o.side} ${o.symbol} ${o.amount} @ ${o.price}") }
    }
}
