package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentType
import com.example.model.InvoiceDocument
import com.example.ui.components.BilloraTopAppBar
import com.example.ui.components.DocumentCard
import com.example.ui.theme.BilloraBlack
import com.example.ui.theme.BilloraCrimson
import com.example.ui.theme.BilloraCrimsonSoft
import com.example.viewmodel.BilloraViewModel
import com.example.viewmodel.DocumentFilter
import com.example.viewmodel.Screen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BilloraViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.filteredDocuments.collectAsState()
    val kpis by viewModel.kpiMetrics.collectAsState()
    val businessProfile by viewModel.businessProfile.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var documentToDelete by remember { mutableStateOf<InvoiceDocument?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BilloraTopAppBar(
                title = "Billora",
                subtitle = "by SimeonJr Studios • ${businessProfile.businessName}",
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.BusinessProfileEdit) },
                        modifier = Modifier.testTag("business_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Business Profile",
                            tint = BilloraCrimson
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Quick action bottom bar for instant Invoice or Receipt creation
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.startNewDocument(DocumentType.INVOICE) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("new_invoice_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BilloraCrimson)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Invoice", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.startNewDocument(DocumentType.RECEIPT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("new_receipt_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BilloraBlack)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp), tint = BilloraCrimson)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Receipt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // KPI Overview Banner
            KpiOverviewBanner(kpis = kpis)

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_input"),
                placeholder = { Text("Search by customer, #, or item...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BilloraCrimson,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs (Horizontal Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BilloraCrimson,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Document List or Empty State
            if (documents.isEmpty()) {
                EmptyStateView(
                    hasQuery = searchQuery.isNotEmpty() || selectedFilter != DocumentFilter.ALL,
                    onNewInvoice = { viewModel.startNewDocument(DocumentType.INVOICE) },
                    onNewReceipt = { viewModel.startNewDocument(DocumentType.RECEIPT) },
                    onResetFilter = {
                        viewModel.setSearchQuery("")
                        viewModel.setFilter(DocumentFilter.ALL)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        DocumentCard(
                            document = doc,
                            onView = { viewModel.navigateTo(Screen.DocumentPreview(doc.id)) },
                            onEdit = { viewModel.editDocument(doc.id) },
                            onDuplicate = { viewModel.duplicateDocument(doc) },
                            onDelete = { documentToDelete = doc },
                            onMarkPaid = { viewModel.markAsPaid(doc) },
                            onShare = {
                                viewModel.loadPreviewDocument(doc.id)
                                viewModel.navigateTo(Screen.DocumentPreview(doc.id))
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    documentToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text("Delete ${doc.docType.title}?") },
            text = { Text("Are you sure you want to delete ${doc.documentNumber} for ${doc.customerName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(doc)
                        documentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun KpiOverviewBanner(kpis: com.example.viewmodel.DashboardKpis) {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BilloraBlack
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FINANCIAL SUMMARY",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFAAAAAA)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Invoiced
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Invoiced",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${kpis.currencySymbol} ${formatter.format(kpis.totalInvoiced)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${kpis.invoiceCount} invoices",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888)
                    )
                }

                // Received
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Received",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${kpis.currencySymbol} ${formatter.format(kpis.totalReceived)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80)
                        )
                    )
                    Text(
                        text = "${kpis.receiptCount} receipts",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888)
                    )
                }

                // Due
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Outstanding Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${kpis.currencySymbol} ${formatter.format(kpis.totalOutstanding)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BilloraCrimson
                        )
                    )
                    Text(
                        text = "unpaid balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    hasQuery: Boolean,
    onNewInvoice: () -> Unit,
    onNewReceipt: () -> Unit,
    onResetFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BilloraCrimsonSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = BilloraCrimson,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasQuery) "No matching documents" else "No invoices or receipts yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasQuery)
                "Try adjusting your search terms or filter."
            else
                "Start generating professional invoices & receipts with automatic totals and instant PDF export.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (hasQuery) {
            OutlinedButton(onClick = onResetFilter) {
                Text("Clear Filters")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNewInvoice,
                    colors = ButtonDefaults.buttonColors(containerColor = BilloraCrimson)
                ) {
                    Text("Create First Invoice")
                }
            }
        }
    }
}
