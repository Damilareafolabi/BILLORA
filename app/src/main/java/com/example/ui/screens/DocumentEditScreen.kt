package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentStatus
import com.example.model.DocumentType
import com.example.model.LineItem
import com.example.model.PaymentMethod
import com.example.ui.components.BilloraTopAppBar
import com.example.ui.components.CurrencySelector
import com.example.ui.theme.BilloraCrimson
import com.example.ui.theme.BilloraCrimsonSoft
import com.example.viewmodel.BilloraViewModel
import com.example.viewmodel.Screen
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DocumentEditScreen(
    viewModel: BilloraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val doc by viewModel.editingDocument.collectAsState()

    BackHandler {
        viewModel.navigateBack()
    }

    val isInvoice = doc.docType.isInvoice

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BilloraTopAppBar(
                title = if (doc.id == 0L) "New ${doc.docType.title}" else "Edit ${doc.docType.title}",
                subtitle = doc.documentNumber,
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveDocument { savedId ->
                                viewModel.loadPreviewDocument(savedId)
                                viewModel.navigateTo(Screen.DocumentPreview(savedId))
                            }
                        },
                        modifier = Modifier.testTag("save_and_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Preview PDF",
                            tint = BilloraCrimson
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Sticky Totals and Save Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = doc.formatAmount(doc.grandTotal),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BilloraCrimson
                                )
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.saveDocument { savedId ->
                                    viewModel.loadPreviewDocument(savedId)
                                    viewModel.navigateTo(Screen.DocumentPreview(savedId))
                                }
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("save_document_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BilloraCrimson),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Preview PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Toggle: Invoice vs Receipt
            item {
                TabRow(
                    selectedTabIndex = if (isInvoice) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = BilloraCrimson
                ) {
                    Tab(
                        selected = isInvoice,
                        onClick = { viewModel.updateEditingDocument { it.copy(docType = DocumentType.INVOICE) } },
                        text = { Text("Invoice", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = !isInvoice,
                        onClick = { viewModel.updateEditingDocument { it.copy(docType = DocumentType.RECEIPT) } },
                        text = { Text("Receipt", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // 1. Basic Metadata: Document # and Dates
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "DOCUMENT DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = BilloraCrimson
                        )

                        OutlinedTextField(
                            value = doc.documentNumber,
                            onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(documentNumber = newVal) } },
                            label = { Text("Document Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_number_input"),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Issue Date Picker
                            val cal = Calendar.getInstance().apply { timeInMillis = doc.issueDate }
                            val datePicker = DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                    viewModel.updateEditingDocument { it.copy(issueDate = newCal.timeInMillis) }
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            )

                            OutlinedTextField(
                                value = doc.formattedIssueDate(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Issue Date") },
                                trailingIcon = {
                                    IconButton(onClick = { datePicker.show() }) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { datePicker.show() }
                            )

                            // Due Date Picker (Invoices) or Receipt Date (Receipts)
                            if (isInvoice) {
                                val dueCal = Calendar.getInstance().apply { timeInMillis = doc.dueDate ?: System.currentTimeMillis() }
                                val dueDatePicker = DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                        viewModel.updateEditingDocument { it.copy(dueDate = newCal.timeInMillis) }
                                    },
                                    dueCal.get(Calendar.YEAR),
                                    dueCal.get(Calendar.MONTH),
                                    dueCal.get(Calendar.DAY_OF_MONTH)
                                )

                                OutlinedTextField(
                                    value = doc.formattedDueDate(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Due Date") },
                                    trailingIcon = {
                                        IconButton(onClick = { dueDatePicker.show() }) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Due Date")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { dueDatePicker.show() }
                                )
                            }
                        }

                        // Currency Selector
                        Text(
                            text = "Currency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CurrencySelector(
                            selectedCode = doc.currencyCode,
                            onCurrencySelected = { code ->
                                viewModel.updateEditingDocument { it.copy(currencyCode = code) }
                            }
                        )
                    }
                }
            }

            // 2. Customer Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (isInvoice) "CUSTOMER INFORMATION" else "RECEIVED FROM (CLIENT)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = BilloraCrimson
                        )

                        OutlinedTextField(
                            value = doc.customerName,
                            onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(customerName = newVal) } },
                            label = { Text("Customer / Business Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_name_input"),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = doc.customerPhone,
                                onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(customerPhone = newVal) } },
                                label = { Text("Phone Number") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("customer_phone_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = doc.customerEmail,
                                onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(customerEmail = newVal) } },
                                label = { Text("Email Address") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("customer_email_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = doc.customerAddress,
                            onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(customerAddress = newVal) } },
                            label = { Text("Customer Address (City / State)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Receipt-Specific Information (Payment Method, Reference, Cashier)
            if (!isInvoice) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "PAYMENT DETAILS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = BilloraCrimson
                            )

                            Text(
                                text = "Payment Method",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentMethod.entries.forEach { method ->
                                    val isSelected = doc.paymentMethod == method
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.updateEditingDocument { it.copy(paymentMethod = method) } }
                                            .background(if (isSelected) BilloraCrimson else MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = method.label,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = doc.transactionReference,
                                onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(transactionReference = newVal) } },
                                label = { Text("Transaction Reference / Bank Session ID") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = doc.receivedBy,
                                onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(receivedBy = newVal) } },
                                label = { Text("Received By / Cashier Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // 4. Line Items Builder
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LINE ITEMS (${doc.items.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { viewModel.addLineItem() },
                        colors = ButtonDefaults.buttonColors(containerColor = BilloraCrimson),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_item_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontSize = 13.sp)
                    }
                }
            }

            itemsIndexed(doc.items, key = { _, item -> item.id }) { index, item ->
                LineItemEditorCard(
                    item = item,
                    currencySymbol = doc.currency.symbol,
                    canDelete = doc.items.size > 1,
                    onUpdate = { updated -> viewModel.updateLineItem(index, updated) },
                    onDelete = { viewModel.removeLineItem(index) }
                )
            }

            // 5. Discount, Tax, and Amount Paid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "DISCOUNT & TAX",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = BilloraCrimson
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = if (doc.discountPercent == 0.0) "" else doc.discountPercent.toString(),
                                onValueChange = { str ->
                                    val pct = str.toDoubleOrNull() ?: 0.0
                                    viewModel.updateEditingDocument { it.copy(discountPercent = pct) }
                                },
                                label = { Text("Overall Discount %") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("discount_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = if (doc.taxPercent == 0.0) "" else doc.taxPercent.toString(),
                                onValueChange = { str ->
                                    val pct = str.toDoubleOrNull() ?: 0.0
                                    viewModel.updateEditingDocument { it.copy(taxPercent = pct) }
                                },
                                label = { Text("VAT / Tax %") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tax_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }

                        if (isInvoice) {
                            OutlinedTextField(
                                value = if (doc.amountPaid == 0.0) "" else doc.amountPaid.toString(),
                                onValueChange = { str ->
                                    val paid = str.toDoubleOrNull() ?: 0.0
                                    viewModel.updateEditingDocument { it.copy(amountPaid = paid) }
                                },
                                label = { Text("Amount Paid in Advance / Deposit (${doc.currency.symbol})") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("amount_paid_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // 6. Notes & Terms
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "NOTES & TERMS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = BilloraCrimson
                        )

                        OutlinedTextField(
                            value = doc.notes,
                            onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(notes = newVal) } },
                            label = { Text("Notes / Payment Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = doc.terms,
                            onValueChange = { newVal -> viewModel.updateEditingDocument { it.copy(terms = newVal) } },
                            label = { Text("Terms & Conditions") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LineItemEditorCard(
    item: LineItem,
    currencySymbol: String,
    canDelete: Boolean,
    onUpdate: (LineItem) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.description,
                    onValueChange = { onUpdate(item.copy(description = it)) },
                    label = { Text("Item / Description *") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_description_input"),
                    singleLine = true
                )

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (item.quantity == 0.0) "" else if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(),
                    onValueChange = { str ->
                        val qty = str.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(quantity = qty))
                    },
                    label = { Text("Qty") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_qty_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                    onValueChange = { str ->
                        val rate = str.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(unitPrice = rate))
                    },
                    label = { Text("Unit Price ($currencySymbol)") },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("item_price_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = if (item.discountPercent == 0.0) "" else item.discountPercent.toString(),
                    onValueChange = { str ->
                        val disc = str.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(discountPercent = disc))
                    },
                    label = { Text("Disc %") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Calculated Item Subtotal
            val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Total: $currencySymbol ${numberFormat.format(item.totalAmount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = BilloraCrimson
                )
            }
        }
    }
}
