package com.ham.exchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ham.exchange.data.model.Coin
import com.ham.exchange.ui.MarketViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MarketsScreen(vm: MarketViewModel, onCoinClick: (String) -> Unit = {}) {
    val coins by vm.coins.collectAsState()
    val loading by vm.loading.collectAsState()
    var query by remember { mutableStateOf("") }
    val filtered = remember(coins, query) {
        if (query.isBlank()) coins else coins.filter { it.name.contains(query, true) || it.symbol.contains(query, true) }
    }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search") }, modifier = Modifier.fillMaxWidth().padding(8.dp))
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn {
            items(filtered) { c -> CoinRow(c, onClick = { onCoinClick(c.id) }) }
        }
    }
}

@Composable
fun CoinRow(c: Coin, onClick: () -> Unit = {}) {
    val nf = NumberFormat.getCurrencyInstance(Locale.US)
    Card(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clickable { onClick() }) {
    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(model = c.image, contentDescription = c.name, modifier = Modifier.size(32.dp))
        Column(Modifier.weight(1f)) {
            Text(c.name, fontWeight = FontWeight.Bold)
            Text(c.symbol, style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(nf.format(c.currentPrice))
            val ch = c.priceChangePercentage24h ?: 0.0
            Text("${"%.2f".format(ch)}%", color = if (ch >= 0) androidx.compose.ui.graphics.Color(0xFF00C853) else androidx.compose.ui.graphics.Color(0xFFFF1744))
        }
    }
    }
}
