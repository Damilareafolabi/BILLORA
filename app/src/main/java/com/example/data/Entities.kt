package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DocumentStatus
import com.example.model.DocumentType
import com.example.model.InvoiceDocument
import com.example.model.LineItem
import com.example.model.PaymentMethod

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val docType: DocumentType,
    val documentNumber: String,
    val createdAt: Long,
    val issueDate: Long,
    val dueDate: Long?,
    val receiptDate: Long?,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val customerAddress: String,
    val items: List<LineItem>,
    val discountPercent: Double,
    val taxPercent: Double,
    val amountPaid: Double,
    val currencyCode: String,
    val status: DocumentStatus,
    val paymentMethod: PaymentMethod?,
    val transactionReference: String,
    val receivedBy: String,
    val notes: String,
    val terms: String,
    val pdfFilePath: String?
) {
    fun toDomain(): InvoiceDocument = InvoiceDocument(
        id = id,
        docType = docType,
        documentNumber = documentNumber,
        createdAt = createdAt,
        issueDate = issueDate,
        dueDate = dueDate,
        receiptDate = receiptDate,
        customerName = customerName,
        customerEmail = customerEmail,
        customerPhone = customerPhone,
        customerAddress = customerAddress,
        items = items,
        discountPercent = discountPercent,
        taxPercent = taxPercent,
        amountPaid = amountPaid,
        currencyCode = currencyCode,
        status = status,
        paymentMethod = paymentMethod,
        transactionReference = transactionReference,
        receivedBy = receivedBy,
        notes = notes,
        terms = terms,
        pdfFilePath = pdfFilePath
    )

    companion object {
        fun fromDomain(domain: InvoiceDocument): DocumentEntity = DocumentEntity(
            id = domain.id,
            docType = domain.docType,
            documentNumber = domain.documentNumber,
            createdAt = domain.createdAt,
            issueDate = domain.issueDate,
            dueDate = domain.dueDate,
            receiptDate = domain.receiptDate,
            customerName = domain.customerName,
            customerEmail = domain.customerEmail,
            customerPhone = domain.customerPhone,
            customerAddress = domain.customerAddress,
            items = domain.items,
            discountPercent = domain.discountPercent,
            taxPercent = domain.taxPercent,
            amountPaid = domain.amountPaid,
            currencyCode = domain.currencyCode,
            status = domain.status,
            paymentMethod = domain.paymentMethod,
            transactionReference = domain.transactionReference,
            receivedBy = domain.receivedBy,
            notes = domain.notes,
            terms = domain.terms,
            pdfFilePath = domain.pdfFilePath
        )
    }
}

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey
    val id: Long = 1L,
    val businessName: String,
    val tagline: String,
    val email: String,
    val phone: String,
    val address: String,
    val website: String,
    val logoUri: String?,
    val taxId: String,
    val bankName: String,
    val accountNumber: String,
    val accountName: String,
    val defaultNotes: String,
    val defaultTerms: String,
    val defaultCurrency: String
)
