package com.example.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InvoiceDocument(
    val id: Long = 0L,
    val docType: DocumentType = DocumentType.INVOICE,
    val documentNumber: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long? = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000,
    val receiptDate: Long? = System.currentTimeMillis(),
    // Customer
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    // Line items
    val items: List<LineItem> = emptyList(),
    // Global discount & tax
    val discountPercent: Double = 0.0,
    val taxPercent: Double = 0.0,
    val amountPaid: Double = 0.0,
    val currencyCode: String = AppCurrency.DEFAULT.code,
    val status: DocumentStatus = DocumentStatus.DRAFT,
    // Receipt specific
    val paymentMethod: PaymentMethod? = PaymentMethod.BANK_TRANSFER,
    val transactionReference: String = "",
    val receivedBy: String = "",
    // Terms & Notes
    val notes: String = "",
    val terms: String = "",
    val pdfFilePath: String? = null
) {
    val currency: AppCurrency
        get() = AppCurrency.fromCode(currencyCode)

    val subtotal: Double
        get() = items.sumOf { it.totalAmount }

    val discountAmount: Double
        get() = subtotal * (discountPercent.coerceIn(0.0, 100.0) / 100.0)

    val afterDiscount: Double
        get() = (subtotal - discountAmount).coerceAtLeast(0.0)

    val taxAmount: Double
        get() = afterDiscount * (taxPercent.coerceIn(0.0, 100.0) / 100.0)

    val grandTotal: Double
        get() = afterDiscount + taxAmount

    val balanceDue: Double
        get() = (grandTotal - amountPaid).coerceAtLeast(0.0)

    val isFullyPaid: Boolean
        get() = amountPaid >= grandTotal && grandTotal > 0

    fun formatAmount(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${currency.symbol} ${formatter.format(amount)}"
    }

    fun formattedIssueDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(issueDate))
    }

    fun formattedDueDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dueDate?.let { sdf.format(Date(it)) } ?: "Upon Receipt"
    }

    fun formattedReceiptDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return receiptDate?.let { sdf.format(Date(it)) } ?: sdf.format(Date(createdAt))
    }
}
