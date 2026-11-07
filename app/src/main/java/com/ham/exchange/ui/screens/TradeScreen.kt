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
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TradeScreen(vm: MarketViewModel, db: HamDatabase, selectedCoinId: String? = null, onSelectedChange: (String?) -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ham", android.content.Context.MODE_PRIVATE) }
    val coinsState = vm.coins.collectAsState()
    val coins = coinsState.value
    var selected by remember { mutableStateOf<String?>(selectedCoinId) }
    LaunchedEffect(selectedCoinId) { selected = selectedCoinId }
    var side by remember { mutableStateOf("BUY") }
    var price by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val coin = coins.firstOrNull { it.id == selected }
    val nf = NumberFormat.getInstance(Locale.US)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Trade", style = MaterialTheme.typography.titleLarge)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(coins) { c ->
                FilterChip(selected = selected == c.id, onClick = { selected = c.id; onSelectedChange(c.id) }, label = { Text(c.symbol) })
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
            val p = price.toBigDecimalOrNull() ?: BigDecimal(c.currentPrice.toString())
            val amt = amount.toBigDecimalOrNull()
            if (amt == null || amt <= BigDecimal.ZERO) { android.widget.Toast.makeText(context, "Jumlah tidak valid", android.widget.Toast.LENGTH_SHORT).show(); return@Button }
            scope.launch {
                val cost = p.multiply(amt)
                val balanceStr = prefs.getString("balance", "100000000000") ?: "100000000000"
                val balance = BigDecimal(balanceStr)
                if (side == "BUY") {
                    if (balance < cost) { android.widget.Toast.makeText(context, "Saldo Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show(); return@launch }
                    prefs.edit().putString("balance", balance.subtract(cost).toPlainString()).apply()
                    val existingList = db.holdings().all().firstOrNull() ?: emptyList()
                    val existing = existingList.firstOrNull { it.coinId == c.id }
                    val newAmount = (existing?.amount ?: BigDecimal.ZERO).add(amt)
                    db.holdings().upsert(HoldingEntity(c.id, c.symbol, newAmount, p))
                } else {
                    val existingList = db.holdings().all().firstOrNull() ?: emptyList()
                    val existing = existingList.firstOrNull { it.coinId == c.id } ?: run { android.widget.Toast.makeText(context, "Tidak ada holding", android.widget.Toast.LENGTH_SHORT).show(); return@launch }
                    if (existing.amount < amt) { android.widget.Toast.makeText(context, "Saldo Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show(); return@launch }
                    val newAmount = existing.amount.subtract(amt)
                    if (newAmount.compareTo(BigDecimal.ZERO) <= 0) db.holdings().delete(c.id) else db.holdings().upsert(HoldingEntity(c.id, c.symbol, newAmount, existing.avgBuyPrice))
                    prefs.edit().putString("balance", balance.add(cost).toPlainString()).apply()
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
    val balance = prefs.getString("balance", "100000000000") ?: "100000000000"
    val nf = NumberFormat.getInstance(Locale.US)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Wallet", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Internal Balance")
                Text("US$${nf.format(BigDecimal(balance))}", style = MaterialTheme.typography.headlineMedium)
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
    val total = holdings.fold(BigDecimal.ZERO) { acc, h -> acc.add(h.amount.multiply(h.avgBuyPrice)) }
    val nf = NumberFormat.getInstance(Locale.US)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Portfolio", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Total Est. Value (USDT)")
                Text("US$${nf.format(total)}", style = MaterialTheme.typography.headlineMedium)
            }
        }
        Text("Holdings")
        holdings.forEach { h ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(h.symbol, style = MaterialTheme.typography.titleMedium)
                    Text("${nf.format(h.amount)} @ ${nf.format(h.avgBuyPrice)}")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Recent Orders")
        orders.take(20).forEach { o ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${o.side} ${o.symbol}", color = if (o.side == "BUY") Color(0xFF00C853) else Color(0xFFFF1744))
                Text("${nf.format(o.amount)} @ ${nf.format(o.price)}")
            }
            HorizontalDivider()
        }
    }
}
