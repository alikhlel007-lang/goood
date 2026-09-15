package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OrderEntity
import com.example.data.repository.CafeRepository
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.Strings
import com.example.ui.viewmodel.CafeViewModel

@Composable
fun CashierOrdersScreen(
    viewModel: CafeViewModel,
    lang: AppLanguage
) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsState()
    val tables by viewModel.tables.collectAsState()

    var selectedStageIndex by remember { mutableIntStateOf(0) } // 0: Approval, 1: Preparing, 2: Delivery, 3: Invoices

    val approvalOrders = allOrders.filter { it.status == "APPROVAL" && !it.isPaid }
    val preparingOrders = allOrders.filter { it.status == "PREPARING" && !it.isPaid }
    val deliveryOrders = allOrders.filter { it.status == "DELIVERY" && !it.isPaid }
    val invoiceOrders = allOrders.filter { (it.status == "INVOICE" || it.status == "DELIVERY") && !it.isPaid }

    var invoiceDialogForTable by remember { mutableStateOf<Int?>(null) }
    var individualReceiptForOrder by remember { mutableStateOf<OrderEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Workflow Stages Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("cashier_orders", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "دورة حياة الطلب: الموافقة ➔ التحضير ➔ التسليم ➔ الفواتير والتسديد",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedStageIndex,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    val stages = listOf(
                        Triple(0, Strings.get("stage_approval", lang), approvalOrders.size),
                        Triple(1, Strings.get("stage_preparing", lang), preparingOrders.size),
                        Triple(2, Strings.get("stage_delivery", lang), deliveryOrders.size),
                        Triple(3, Strings.get("stage_invoices", lang), invoiceOrders.size)
                    )

                    stages.forEach { (idx, title, count) ->
                        Tab(
                            selected = selectedStageIndex == idx,
                            onClick = { selectedStageIndex = idx },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                ) {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (count > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Badge(containerColor = if (selectedStageIndex == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary) {
                                            Text("$count")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stage Content
        when (selectedStageIndex) {
            0 -> {
                // Stage 1: Approval
                OrderListStage(
                    orders = approvalOrders,
                    lang = lang,
                    emptyText = Strings.get("no_orders_yet", lang),
                    actionButtonText = Strings.get("accept_order", lang),
                    onAction = { order ->
                        viewModel.updateOrderStatus(order.id, "PREPARING")
                        Toast.makeText(context, "تمت الموافقة وبدء التحضير", Toast.LENGTH_SHORT).show()
                    },
                    secondaryButtonText = Strings.get("reject_order", lang),
                    onSecondaryAction = { order ->
                        viewModel.updateOrderStatus(order.id, "REJECTED")
                    }
                )
            }
            1 -> {
                // Stage 2: Preparing
                OrderListStage(
                    orders = preparingOrders,
                    lang = lang,
                    emptyText = Strings.get("no_orders_yet", lang),
                    actionButtonText = Strings.get("mark_ready", lang),
                    onAction = { order ->
                        viewModel.updateOrderStatus(order.id, "DELIVERY")
                        Toast.makeText(context, "الطلب جاهز للتسليم", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            2 -> {
                // Stage 3: Delivery
                OrderListStage(
                    orders = deliveryOrders,
                    lang = lang,
                    emptyText = Strings.get("no_orders_yet", lang),
                    actionButtonText = Strings.get("move_to_invoices", lang),
                    onAction = { order ->
                        viewModel.updateOrderStatus(order.id, "INVOICE")
                        Toast.makeText(context, "تم التسليم وتحويل الطلب للفاتورة", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            3 -> {
                // Stage 4: Invoicing & Table Grouping (Split vs Combined)
                TableInvoicesStage(
                    unpaidOrders = invoiceOrders,
                    tables = tables,
                    viewModel = viewModel,
                    lang = lang,
                    onOpenTableBilling = { tableNum ->
                        invoiceDialogForTable = tableNum
                    }
                )
            }
        }

        // Table Invoicing Dialog (Allows paying individually per person or whole table)
        if (invoiceDialogForTable != null) {
            val tableNum = invoiceDialogForTable!!
            val tableOrders = invoiceOrders.filter { it.tableNumber == tableNum }
            val tableSubtotal = tableOrders.sumOf { it.subtotal }
            // Combined service fee 1% min 250 IQD (rounded to nearest 250 IQD)
            val combinedServiceFee = CafeRepository.calculateServiceFee(tableSubtotal)
            val combinedTotal = CafeRepository.roundTo250IQD(tableSubtotal + combinedServiceFee)
            val groupedByCustomer = tableOrders.groupBy { it.customerName.trim().ifEmpty { "زبون" } }.toList()

            AlertDialog(
                onDismissRequest = { invoiceDialogForTable = null },
                icon = {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "فاتورة مجمعة: ${Strings.get("table", lang)} #$tableNum",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "الطلبات الفردية لرواد هذه الطاولة:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        items(groupedByCustomer) { (customerName, custOrders) ->
                            val custSubtotal = custOrders.sumOf { it.subtotal }
                            val indivFee = CafeRepository.calculateServiceFee(custSubtotal)
                            val indivTotal = CafeRepository.roundTo250IQD(custSubtotal + indivFee)
                            val allItemsSummary = custOrders.joinToString("\n") { it.itemsSummary }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(customerName, fontWeight = FontWeight.Bold)
                                            if (custOrders.size > 1) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("(${custOrders.size} طلبات مجمعة معاً)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Text("${custSubtotal.toInt()} ${Strings.get("iqd", lang)}", fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        allItemsSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        "رسوم فردية (1% أدنى 250): ${indivFee.toInt()} د.ع | الإجمالي (مضاعفات 250): ${indivTotal.toInt()} د.ع",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = {
                                            custOrders.forEach { viewModel.payIndividualOrder(it.id) }
                                            Toast.makeText(context, "تم تسديد حساب $customerName بنجاح", Toast.LENGTH_SHORT).show()
                                            if (groupedByCustomer.size <= 1) {
                                                invoiceDialogForTable = null
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${Strings.get("pay_individual", lang)} (${indivTotal.toInt()} د.ع)", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Text(
                                "المجموع الكلي للطاولة عند التسديد المشترك:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مجموع المواد:")
                                Text("${tableSubtotal.toInt()} د.ع")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("رسوم 1% على المجموع (أدنى 250):")
                                Text("${combinedServiceFee.toInt()} د.ع", color = MaterialTheme.colorScheme.primary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الإجمالي النهائي (مضاعفات 250):", fontWeight = FontWeight.Bold)
                                Text("${combinedTotal.toInt()} د.ع", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.payWholeTable(tableNum)
                            Toast.makeText(context, "تم تسديد كامل طاولة #$tableNum بنجاح!", Toast.LENGTH_SHORT).show()
                            invoiceDialogForTable = null
                        }
                    ) {
                        Text("${Strings.get("pay_whole_table", lang)} (${combinedTotal.toInt()} د.ع)")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { invoiceDialogForTable = null }) {
                        Text(Strings.get("cancel_dismiss", lang))
                    }
                }
            )
        }
    }
}

@Composable
fun OrderListStage(
    orders: List<OrderEntity>,
    lang: AppLanguage,
    emptyText: String,
    actionButtonText: String,
    onAction: (OrderEntity) -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryAction: ((OrderEntity) -> Unit)? = null
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.HourglassTop,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${Strings.get("table", lang)} #${order.tableNumber}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(order.customerName, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = "${order.totalAmount.toInt()} ${Strings.get("iqd", lang)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Items Summary
                        Text(
                            text = order.itemsSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (order.orderNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ملاحظة الزبون: ${order.orderNotes}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (secondaryButtonText != null && onSecondaryAction != null) {
                                OutlinedButton(
                                    onClick = { onSecondaryAction(order) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(secondaryButtonText, color = MaterialTheme.colorScheme.error)
                                }
                            }

                            Button(
                                onClick = { onAction(order) },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(actionButtonText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableInvoicesStage(
    unpaidOrders: List<OrderEntity>,
    tables: List<com.example.data.local.entity.TableEntity>,
    viewModel: CafeViewModel,
    lang: AppLanguage,
    onOpenTableBilling: (Int) -> Unit
) {
    val context = LocalContext.current
    var invoiceGroupMode by remember { mutableIntStateOf(0) } // 0: By Customer (تجميع فواتير الزبون معاً), 1: By Table (حسب الطاولات)

    if (unpaidOrders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DoneAll,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Strings.get("all_paid_success", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Grouping Mode Selector Tabs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                TabRow(
                    selectedTabIndex = invoiceGroupMode,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = invoiceGroupMode == 0,
                        onClick = { invoiceGroupMode = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تجميع فواتير الزبون معاً", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = invoiceGroupMode == 1,
                        onClick = { invoiceGroupMode = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TableRestaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حسب الطاولات", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }

            if (invoiceGroupMode == 0) {
                // Grouped by Customer (e.g., customer "n" combined invoices)
                val customerGroups = unpaidOrders.groupBy { it.customerName.trim().ifEmpty { "زبون" } }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    customerGroups.forEach { (custName, custOrders) ->
                        val custSubtotal = custOrders.sumOf { it.subtotal }
                        val indivFee = CafeRepository.calculateServiceFee(custSubtotal)
                        val totalAmount = CafeRepository.roundTo250IQD(custSubtotal + indivFee)
                        val tablesString = custOrders.map { it.tableNumber }.distinct().sorted().joinToString(", ") { "#$it" }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "فاتورة الزبون: $custName",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "طاولة: $tablesString",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = if (custOrders.size > 1) "${custOrders.size} طلبات مجمعة معاً" else "طلب واحد",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                                    // Items Ordered Breakdown
                                    Text(
                                        text = "المواد المطلوبة:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    custOrders.forEachIndexed { idx, order ->
                                        if (custOrders.size > 1) {
                                            Text(
                                                text = "• طلب #${idx + 1} (طاولة #${order.tableNumber}): ${order.itemsSummary}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "• ${order.itemsSummary}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                                    // Bill Totals (Constraint: always multiple of 250 IQD)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("مجموع المواد:", style = MaterialTheme.typography.bodySmall)
                                        Text("${custSubtotal.toInt()} د.ع", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("رسوم الخدمة 1% (أدنى 250):", style = MaterialTheme.typography.bodySmall)
                                        Text("${indivFee.toInt()} د.ع", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الإجمالي النهائي:",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${totalAmount.toInt()} ${Strings.get("iqd", lang)}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            custOrders.forEach { viewModel.payIndividualOrder(it.id) }
                                            Toast.makeText(context, "تم تسديد فاتورة $custName بالكامل بنجاح!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تسديد فاتورة $custName بالكامل (${totalAmount.toInt()} د.ع)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Grouped by Tables
                val tableGroups = unpaidOrders.groupBy { it.tableNumber }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    tableGroups.forEach { (tableNum, orders) ->
                        val tableSubtotal = orders.sumOf { it.subtotal }
                        val combinedFee = CafeRepository.calculateServiceFee(tableSubtotal)
                        val grandTotal = CafeRepository.roundTo250IQD(tableSubtotal + combinedFee)
                        val tableObj = tables.firstOrNull { it.tableNumber == tableNum }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.TableRestaurant,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "${Strings.get("table", lang)} #$tableNum",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                val typeText = when (tableObj?.tableType) {
                                                    "VIP" -> "VIP Lounge"
                                                    "OUTDOOR" -> "جلسة خارجية"
                                                    else -> "جلسة داخلية"
                                                }
                                                Text(
                                                    text = typeText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "${orders.size} طلبات نشطة",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                                    // Individual Customer Orders Breakdown (Grouped by customer)
                                    val customerGroups = orders.groupBy { it.customerName.trim().ifEmpty { "زبون" } }
                                    customerGroups.forEach { (custName, custOrders) ->
                                        val custSubtotal = custOrders.sumOf { it.subtotal }
                                        val indivFee = CafeRepository.calculateServiceFee(custSubtotal)
                                        val indivTotal = CafeRepository.roundTo250IQD(custSubtotal + indivFee)
                                        val allItems = custOrders.joinToString(", ") { it.itemsSummary }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(custName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    if (custOrders.size > 1) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("(${custOrders.size} مجمعة)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                Text(
                                                    allItems,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("${indivTotal.toInt()} د.ع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("(رسوم ${indivFee.toInt()} د.ع)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("المجموع الكلي للطاولة:", style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                "${grandTotal.toInt()} ${Strings.get("iqd", lang)}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }

                                        Button(
                                            onClick = { onOpenTableBilling(tableNum) },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("تفاصيل ودفع الفاتورة", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
