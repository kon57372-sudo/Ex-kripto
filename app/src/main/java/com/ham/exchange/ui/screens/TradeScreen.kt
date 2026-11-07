package com.ham.exchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var price by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val coin = coins.firstOrNull { it.id == selected }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Trade", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            coins.take(6).forEach { c ->
                FilterChip(selected = selected == c.id, onClick = { selected = c.id }, label = { Text(c.symbol) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = side == "BUY", onClick = { side = "BUY" }, label = { Text("BUY") })
            FilterChip(selected = side == "SELL", onClick = { side = "SELL" }, label = { Text("SELL") })
        }
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val c = coin ?: run { android.widget.Toast.makeText(context, "Pilih koin dulu", android.widget.Toast.LENGTH_SHORT).show(); return@Button }
            val p = price.toDoubleOrNull() ?: c.currentPrice
            val amt = amount.toDoubleOrNull()
            if (amt == null || amt <= 0) { android.widget.Toast.makeText(context, "Jumlah tidak valid", android.widget.Toast.LENGTH_SHORT).show(); return@Button }
            scope.launch {
                val cost = p * amt
                val balance = prefs.getFloat("balance", 100000000000f).toDouble()
                if (side == "BUY") {
                    if (balance < cost) { android.widget.Toast.makeText(context, "Saldo Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show(); return@launch }
                    prefs.edit().putFloat("balance", (balance - cost).toFloat()).apply()
                    val existingList = db.holdings().all().firstOrNull() ?: emptyList()
                    val existing = existingList.firstOrNull { it.coinId == c.id }
                    val newAmount = (existing?.amount ?: 0.0) + amt
                    db.holdings().upsert(HoldingEntity(c.id, c.symbol, newAmount, p))
                } else {
                    val existingList = db.holdings().all().firstOrNull() ?: emptyList()
                    val existing = existingList.firstOrNull { it.coinId == c.id } ?: run { android.widget.Toast.makeText(context, "Tidak ada holding", android.widget.Toast.LENGTH_SHORT).show(); return@launch }
                    if (existing.amount < amt) { android.widget.Toast.makeText(context, "Saldo Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show(); return@launch }
                    val newAmount = existing.amount - amt
                    if (newAmount <= 0.0) db.holdings().delete(c.id) else db.holdings().upsert(HoldingEntity(c.id, c.symbol, newAmount, existing.avgBuyPrice))
                    prefs.edit().putFloat("balance", (balance + cost).toFloat()).apply()
                }
                db.orders().insert(OrderEntity(coinId = c.id, symbol = c.symbol, side = side, type = "MARKET", price = p, amount = amt, status = "FILLED", timestamp = System.currentTimeMillis()))
                android.widget.Toast.makeText(context, if (side == "BUY") "Pembelian Berhasil" else "Penjualan Berhasil", android.widget.Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (side == "BUY") Color(0xFF00C853) else Color(0xFFFF1744))) {
            Text(if (side == "BUY") "Buy ${coin?.symbol ?: ""}" else "Sell ${coin?.symbol ?: ""}")
        }
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
