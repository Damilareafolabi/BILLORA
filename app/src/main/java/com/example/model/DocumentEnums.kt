package com.example.model

enum class DocumentType(val title: String, val prefix: String) {
    INVOICE("Invoice", "INV"),
    RECEIPT("Receipt", "REC");

    val isInvoice: Boolean get() = this == INVOICE
    val isReceipt: Boolean get() = this == RECEIPT
}

enum class DocumentStatus(val label: String) {
    DRAFT("Draft"),
    PENDING("Pending"),
    SENT("Sent"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    CARD("Debit/Credit Card"),
    POS("POS"),
    CHEQUE("Cheque"),
    ONLINE("Online / Transfer"),
    OTHER("Other");

    companion object {
        fun fromLabel(label: String?): PaymentMethod {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: BANK_TRANSFER
        }
    }
}
