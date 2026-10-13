package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BilloraDatabase
import com.example.data.DocumentRepository
import com.example.model.AppCurrency
import com.example.model.BusinessProfile
import com.example.model.DocumentStatus
import com.example.model.DocumentType
import com.example.model.InvoiceDocument
import com.example.model.LineItem
import com.example.model.PaymentMethod
import com.example.pdf.PdfGenerator
import com.example.pdf.PdfShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class Screen {
    data object Welcome : Screen()
    data object Home : Screen()
    data class DocumentEdit(val documentId: Long = 0L, val initialType: DocumentType = DocumentType.INVOICE) : Screen()
    data class DocumentPreview(val documentId: Long) : Screen()
    data object BusinessProfileEdit : Screen()
    data object Settings : Screen()
}

enum class DocumentFilter(val label: String) {
    ALL("All"),
    INVOICES("Invoices"),
    RECEIPTS("Receipts"),
    PAID("Paid"),
    PENDING("Pending"),
    OVERDUE("Overdue")
}

data class DashboardKpis(
    val totalInvoiced: Double = 0.0,
    val totalReceived: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val invoiceCount: Int = 0,
    val receiptCount: Int = 0,
    val currencySymbol: String = "₦"
)

class BilloraViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("billora_prefs", android.content.Context.MODE_PRIVATE)
    private val database = BilloraDatabase.getDatabase(application)
    val repository = DocumentRepository(database.documentDao(), database.businessProfileDao())

    // Navigation Stack: Show Welcome page first if not seen yet
    private val _currentScreen = MutableStateFlow<Screen>(
        if (prefs.getBoolean("has_seen_welcome", false)) Screen.Home else Screen.Welcome
    )
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DocumentFilter.ALL)
    val selectedFilter: StateFlow<DocumentFilter> = _selectedFilter.asStateFlow()

    // Business Profile
    val businessProfile: StateFlow<BusinessProfile> = repository.businessProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = BusinessProfile()
        )

    // All Documents from Database
    val allDocuments: StateFlow<List<InvoiceDocument>> = repository.allDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered Documents
    val filteredDocuments: StateFlow<List<InvoiceDocument>> = combine(
        allDocuments,
        _searchQuery,
        _selectedFilter
    ) { docs, query, filter ->
        docs.filter { doc ->
            val matchesQuery = query.isBlank() ||
                    doc.customerName.contains(query, ignoreCase = true) ||
                    doc.documentNumber.contains(query, ignoreCase = true) ||
                    doc.items.any { it.description.contains(query, ignoreCase = true) }

            val matchesFilter = when (filter) {
                DocumentFilter.ALL -> true
                DocumentFilter.INVOICES -> doc.docType.isInvoice
                DocumentFilter.RECEIPTS -> doc.docType.isReceipt
                DocumentFilter.PAID -> doc.status == DocumentStatus.PAID
                DocumentFilter.PENDING -> doc.status == DocumentStatus.DRAFT || doc.status == DocumentStatus.SENT
                DocumentFilter.OVERDUE -> doc.status == DocumentStatus.OVERDUE
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // KPIs Dashboard Calculation
    val kpiMetrics: StateFlow<DashboardKpis> = combine(allDocuments, businessProfile) { docs, profile ->
        val symbol = AppCurrency.fromCode(profile.defaultCurrency).symbol
        var invoiced = 0.0
        var received = 0.0
        var outstanding = 0.0
        var invCount = 0
        var recCount = 0

        docs.forEach { doc ->
            if (doc.docType.isInvoice) {
                invCount++
                invoiced += doc.grandTotal
                outstanding += doc.balanceDue
                received += doc.amountPaid
            } else {
                recCount++
                received += doc.grandTotal
            }
        }

        DashboardKpis(
            totalInvoiced = invoiced,
            totalReceived = received,
            totalOutstanding = outstanding,
            invoiceCount = invCount,
            receiptCount = recCount,
            currencySymbol = symbol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardKpis()
    )

    // Document Editing Form State
    private val _editingDocument = MutableStateFlow(InvoiceDocument())
    val editingDocument: StateFlow<InvoiceDocument> = _editingDocument.asStateFlow()

    // Preview Screen State
    private val _previewDocument = MutableStateFlow<InvoiceDocument?>(null)
    val previewDocument: StateFlow<InvoiceDocument?> = _previewDocument.asStateFlow()

    private val _previewPdfFile = MutableStateFlow<File?>(null)
    val previewPdfFile: StateFlow<File?> = _previewPdfFile.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _isPreviewLoading = MutableStateFlow(false)
    val isPreviewLoading: StateFlow<Boolean> = _isPreviewLoading.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen is Screen.DocumentPreview) {
            loadPreviewDocument(screen.documentId)
        }
    }

    fun navigateBack() {
        _currentScreen.value = Screen.Home
    }

    fun completeWelcome(selectedCurrency: String? = null, setupProfileNow: Boolean = false) {
        prefs.edit().putBoolean("has_seen_welcome", true).apply()
        if (selectedCurrency != null) {
            viewModelScope.launch {
                val currentProfile = repository.getBusinessProfileDirect()
                repository.saveBusinessProfile(currentProfile.copy(defaultCurrency = selectedCurrency))
            }
        }
        if (setupProfileNow) {
            _currentScreen.value = Screen.BusinessProfileEdit
        } else {
            _currentScreen.value = Screen.Home
        }
    }

    fun showWelcomeScreen() {
        _currentScreen.value = Screen.Welcome
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: DocumentFilter) {
        _selectedFilter.value = filter
    }

    fun startNewDocument(type: DocumentType) {
        viewModelScope.launch {
            val nextNumber = repository.generateNextDocumentNumber(type)
            val profile = repository.getBusinessProfileDirect()
            _editingDocument.value = InvoiceDocument(
                id = 0L,
                docType = type,
                documentNumber = nextNumber,
                currencyCode = profile.defaultCurrency,
                status = if (type.isReceipt) DocumentStatus.PAID else DocumentStatus.DRAFT,
                amountPaid = 0.0,
                notes = profile.defaultNotes,
                terms = profile.defaultTerms,
                items = listOf(
                    LineItem(description = "Professional Services", quantity = 1.0, unitPrice = 0.0)
                )
            )
            _currentScreen.value = Screen.DocumentEdit(0L, type)
        }
    }

    fun editDocument(id: Long) {
        viewModelScope.launch {
            val doc = repository.getDocumentByIdDirect(id)
            if (doc != null) {
                _editingDocument.value = doc
                _currentScreen.value = Screen.DocumentEdit(id, doc.docType)
            }
        }
    }

    fun updateEditingDocument(transform: (InvoiceDocument) -> InvoiceDocument) {
        _editingDocument.value = transform(_editingDocument.value)
    }

    // Line items manipulation in editor
    fun addLineItem() {
        val currentItems = _editingDocument.value.items.toMutableList()
        currentItems.add(LineItem(description = "", quantity = 1.0, unitPrice = 0.0))
        _editingDocument.value = _editingDocument.value.copy(items = currentItems)
    }

    fun updateLineItem(index: Int, updated: LineItem) {
        val currentItems = _editingDocument.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = updated
            _editingDocument.value = _editingDocument.value.copy(items = currentItems)
        }
    }

    fun removeLineItem(index: Int) {
        val currentItems = _editingDocument.value.items.toMutableList()
        if (index in currentItems.indices && currentItems.size > 1) {
            currentItems.removeAt(index)
            _editingDocument.value = _editingDocument.value.copy(items = currentItems)
        }
    }

    fun saveDocument(onSuccess: (Long) -> Unit) {
        val current = _editingDocument.value
        if (current.customerName.isBlank()) {
            Toast.makeText(getApplication(), "Please enter a customer name", Toast.LENGTH_SHORT).show()
            return
        }
        if (current.items.isEmpty()) {
            Toast.makeText(getApplication(), "Please add at least one line item", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            // Auto update amountPaid for receipts if it was 0
            val docToSave = if (current.docType.isReceipt && current.amountPaid <= 0) {
                current.copy(amountPaid = current.grandTotal)
            } else current

            val savedId = repository.saveDocument(docToSave)
            Toast.makeText(getApplication(), "${current.docType.title} saved successfully", Toast.LENGTH_SHORT).show()
            onSuccess(savedId)
        }
    }

    fun duplicateDocument(document: InvoiceDocument) {
        viewModelScope.launch {
            val newId = repository.duplicateDocument(document)
            Toast.makeText(getApplication(), "${document.docType.title} duplicated!", Toast.LENGTH_SHORT).show()
            loadPreviewDocument(newId)
            _currentScreen.value = Screen.DocumentPreview(newId)
        }
    }

    fun deleteDocument(document: InvoiceDocument) {
        viewModelScope.launch {
            repository.deleteDocument(document.id, document.pdfFilePath)
            Toast.makeText(getApplication(), "${document.docType.title} deleted", Toast.LENGTH_SHORT).show()
            if (_currentScreen.value is Screen.DocumentPreview) {
                _currentScreen.value = Screen.Home
            }
        }
    }

    fun markAsPaid(document: InvoiceDocument) {
        viewModelScope.launch {
            repository.updateDocumentStatus(document.id, DocumentStatus.PAID, document.grandTotal)
            Toast.makeText(getApplication(), "Marked as Paid!", Toast.LENGTH_SHORT).show()
            if (_previewDocument.value?.id == document.id) {
                loadPreviewDocument(document.id)
            }
        }
    }

    fun loadPreviewDocument(documentId: Long) {
        viewModelScope.launch {
            _isPreviewLoading.value = true
            val doc = repository.getDocumentByIdDirect(documentId)
            _previewDocument.value = doc

            if (doc != null) {
                val profile = repository.getBusinessProfileDirect()
                withContext(Dispatchers.IO) {
                    val file = PdfGenerator.generatePdf(getApplication(), doc, profile)
                    _previewPdfFile.value = file

                    // Update saved pdf file path in db
                    repository.saveDocument(doc.copy(pdfFilePath = file.absolutePath))

                    val bitmap = PdfShareHelper.renderPdfPageToBitmap(file)
                    _previewBitmap.value = bitmap
                }
            }
            _isPreviewLoading.value = false
        }
    }

    fun shareCurrentPdf() {
        val file = _previewPdfFile.value
        val doc = _previewDocument.value
        if (file != null && doc != null) {
            val subject = "${doc.docType.title} #${doc.documentNumber} from ${businessProfile.value.businessName}"
            PdfShareHelper.sharePdf(getApplication(), file, subject)
        } else {
            Toast.makeText(getApplication(), "Generating PDF, please wait...", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCurrentPdf() {
        val file = _previewPdfFile.value
        if (file != null) {
            PdfShareHelper.openPdf(getApplication(), file)
        } else {
            Toast.makeText(getApplication(), "Generating PDF, please wait...", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveBusinessProfile(profile)
            Toast.makeText(getApplication(), "Business profile updated!", Toast.LENGTH_SHORT).show()
            _currentScreen.value = Screen.Home
        }
    }
}
