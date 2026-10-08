package io.gothub.hosseinkarami_dev.penny

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.gothub.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.gothub.hosseinkarami_dev.penny.domain.model.TransactionType
import io.gothub.hosseinkarami_dev.penny.domain.usecase.SaveTransaction
import io.gothub.hosseinkarami_dev.penny.presentation.DashboardViewModel
import kotlin.time.Clock
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

private val Ink = Color(0xFF10251F)
private val Muted = Color(0xFF84918B)
private val Canvas = Color(0xFFF5F7F4)
private val Green = Color(0xFF13795B)
private val Mint = Color(0xFFDDF4E9)
private val Coral = Color(0xFFE7775B)

@Composable
fun App() {
    KoinApplication(application = { modules(io.gothub.hosseinkarami_dev.penny.di.appModule) }) {
        MaterialTheme(colorScheme = lightColorScheme(primary = Green, onPrimary = Color.White, background = Canvas, surface = Color.White, onSurface = Ink)) {
            DashboardScreen()
        }
    }
}

@Composable
private fun DashboardScreen(viewModel: DashboardViewModel = koinInject(), saveTransaction: SaveTransaction = koinInject()) {
    val summary by viewModel.summary.collectAsState()
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Home") }
    Scaffold(containerColor = Canvas, bottomBar = {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 24.dp, vertical = 11.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("⌂" to "Home", "↕" to "Activity", "◷" to "Insights", "◎" to "Profile").forEach { (icon, label) ->
                Column(Modifier.clickable { selectedTab = label }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(icon, color = if (selectedTab == label) Green else Muted, fontSize = 21.sp)
                    Text(label, color = if (selectedTab == label) Green else Muted, fontSize = 10.sp)
                }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Penny", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green); Text("YOUR MONEY, IN BALANCE", fontSize = 9.sp, letterSpacing = 1.5.sp, color = Muted) }
                Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Text("●", color = Green, fontSize = 20.sp) } }
            }
            Spacer(Modifier.height(25.dp))
            BalanceCard(summary.balanceMinor)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Income", summary.incomeMinor, "↗", Modifier.weight(1f), Green)
                SummaryCard("Expenses", summary.expenseMinor, "↘", Modifier.weight(1f), Coral)
            }
            Spacer(Modifier.height(25.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (selectedTab == "Activity") "All activity" else "Recent activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("See all  →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Green, modifier = Modifier.clickable { selectedTab = "Activity" })
            }
            Spacer(Modifier.height(12.dp))
            if (summary.recentTransactions.isEmpty()) EmptyState() else summary.recentTransactions.forEach { TransactionRow(it); Spacer(Modifier.height(8.dp)) }
            Spacer(Modifier.height(20.dp))
            InsightsCard(summary.expenseMinor, summary.incomeMinor)
            Spacer(Modifier.height(22.dp))
        }
    }
    if (showAdd) AddTransactionDialog(onDismiss = { showAdd = false }, onSave = { transaction ->
        scope.launch { runCatching { saveTransaction(transaction) }.onSuccess { showAdd = false } }
    })
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Button(onClick = { showAdd = true }, modifier = Modifier.padding(end = 22.dp, bottom = 78.dp).size(58.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light) }
    }
}

@Composable
private fun BalanceCard(balance: Long) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Ink), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("TOTAL BALANCE", color = Color(0xFFB7C9C1), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold); Text("•••", color = Color(0xFFB7C9C1), fontSize = 18.sp) }
            Spacer(Modifier.height(10.dp)); Text(formatMoney(balance), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Spacer(Modifier.height(19.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFF21483A), shape = RoundedCornerShape(20.dp)) { Text("  LOCAL ONLY  ", color = Color(0xFF90E0B5), fontSize = 10.sp, modifier = Modifier.padding(vertical = 5.dp)) }
                Spacer(Modifier.width(8.dp)); Text("Your data stays on this device", color = Color(0xFFB7C9C1), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Long, icon: String, modifier: Modifier, accent: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = Muted, fontSize = 12.sp)
                Surface(shape = CircleShape, color = if (accent == Green) Mint else Color(0xFFFFEAE5), modifier = Modifier.size(30.dp)) { Box(contentAlignment = Alignment.Center) { Text(icon, color = accent, fontSize = 16.sp) } }
            }
            Spacer(Modifier.height(9.dp)); Text(formatMoney(amount), color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("All time", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun EmptyState() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 26.dp, horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = Mint, modifier = Modifier.size(52.dp)) { Box(contentAlignment = Alignment.Center) { Text("✦", color = Green, fontSize = 22.sp) } }
            Spacer(Modifier.height(11.dp)); Text("Your money story starts here", fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.height(4.dp)); Text("Add your first transaction to see your activity.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TransactionRow(transaction: MoneyTransaction) {
    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = if (transaction.type == TransactionType.INCOME) Mint else Color(0xFFF1F2EF), modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Text(categoryMark(transaction.category), fontSize = 18.sp) } }
        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(transaction.title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(transaction.category, color = Muted, fontSize = 11.sp) }
        Text((if (transaction.type == TransactionType.INCOME) "+ " else "− ") + formatMoney(transaction.amountMinor), color = if (transaction.type == TransactionType.INCOME) Green else Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InsightsCard(expense: Long, income: Long) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text("Cash flow", color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text("Income and expenses at a glance", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(17.dp)); MetricBar("Income", income, income, Green)
            Spacer(Modifier.height(12.dp)); MetricBar("Expenses", expense, income, Coral)
            Spacer(Modifier.height(12.dp)); Text(if (income == 0L && expense == 0L) "Insights will appear as you log activity." else "You're building a clearer picture of your finances.", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MetricBar(label: String, value: Long, total: Long, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Muted, fontSize = 11.sp); Text(formatMoney(value), color = Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
    Spacer(Modifier.height(6.dp)); LinearProgressIndicator(progress = { if (total == 0L) 0f else (value.toFloat() / total).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), color = color, trackColor = color.copy(alpha = .12f))
}

@Composable
private fun AddTransactionDialog(onDismiss: () -> Unit, onSave: (MoneyTransaction) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add transaction", fontWeight = FontWeight.Bold) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TransactionType.entries.forEach { option ->
                Button(onClick = { type = option }, colors = ButtonDefaults.buttonColors(containerColor = if (type == option) Green else Canvas)) { Text(if (option == TransactionType.EXPENSE) "Expense" else "Income", color = if (type == option) Color.White else Ink) }
            } }
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Description") }, singleLine = true)
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Amount (USD)") }, singleLine = true)
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true)
            error?.let { Text(it, color = Coral, fontSize = 12.sp) }
        }
    }, confirmButton = { TextButton(onClick = {
        val parsedMinor = amount.toMinorUnits()
        when { title.isBlank() -> error = "Add a description."; parsedMinor == null || parsedMinor <= 0 -> error = "Enter an amount greater than zero."; category.isBlank() -> error = "Add a category."; else -> {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            onSave(MoneyTransaction(timestamp.toString(), title.trim(), parsedMinor, type, category.trim(), timestamp))
        } }
    }) { Text("Save transaction") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Muted) } })
}

private fun formatMoney(minor: Long): String {
    val sign = if (minor < 0) "−" else ""
    val amount = kotlin.math.abs(minor)
    return "${sign}\$${amount / 100}.${(amount % 100).toString().padStart(2, '0')}"
}

private fun String.toMinorUnits(): Long? {
    val parts = trim().split('.')
    if (parts.size > 2 || parts[0].isEmpty() || parts[0].any { !it.isDigit() }) return null
    val fraction = parts.getOrNull(1).orEmpty()
    if (fraction.length > 2 || fraction.any { !it.isDigit() }) return null
    val whole = parts[0].toLongOrNull() ?: return null
    val cents = fraction.padEnd(2, '0').ifEmpty { "00" }.toLongOrNull() ?: return null
    if (whole > (Long.MAX_VALUE - cents) / 100) return null
    return whole * 100 + cents
}

private fun categoryMark(category: String): String = when (category.lowercase()) {
    "food", "groceries", "dining" -> "◉"
    "transport", "travel" -> "↗"
    "shopping" -> "◇"
    "salary", "income" -> "↙"
    else -> "•"
}
