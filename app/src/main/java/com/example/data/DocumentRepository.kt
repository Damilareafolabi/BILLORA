package com.example.data

import com.example.model.AppCurrency
import com.example.model.BusinessProfile
import com.example.model.DocumentStatus
import com.example.model.DocumentType
import com.example.model.InvoiceDocument
import com.example.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val profileDao: BusinessProfileDao
) {
    val allDocuments: Flow<List<InvoiceDocument>> =
        documentDao.getAllDocuments().map { list -> list.map { it.toDomain() } }

    val businessProfile: Flow<BusinessProfile> =
        profileDao.getProfile().map { entity ->
            entity?.let {
                BusinessProfile(
                    id = it.id,
                    businessName = it.businessName,
                    tagline = it.tagline,
                    email = it.email,
                    phone = it.phone,
                    address = it.address,
                    website = it.website,
                    logoUri = it.logoUri,
                    taxId = it.taxId,
                    bankName = it.bankName,
                    accountNumber = it.accountNumber,
                    accountName = it.accountName,
                    defaultNotes = it.defaultNotes,
                    defaultTerms = it.defaultTerms,
                    defaultCurrency = it.defaultCurrency
                )
            } ?: defaultBusinessProfile()
        }

    fun getDocumentById(id: Long): Flow<InvoiceDocument?> =
        documentDao.getDocumentById(id).map { it?.toDomain() }

    suspend fun getDocumentByIdDirect(id: Long): InvoiceDocument? =
        documentDao.getDocumentByIdDirect(id)?.toDomain()

    suspend fun saveDocument(doc: InvoiceDocument): Long {
        val entity = DocumentEntity.fromDomain(doc)
        return if (doc.id == 0L) {
            documentDao.insertDocument(entity)
        } else {
            documentDao.updateDocument(entity)
            doc.id
        }
    }

    suspend fun deleteDocument(id: Long, pdfPath: String? = null) {
        documentDao.deleteDocumentById(id)
        pdfPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
    }

    suspend fun duplicateDocument(original: InvoiceDocument): Long {
        val newDocNumber = generateNextDocumentNumber(original.docType)
        val copy = original.copy(
            id = 0L,
            documentNumber = newDocNumber,
            createdAt = System.currentTimeMillis(),
            issueDate = System.currentTimeMillis(),
            dueDate = if (original.dueDate != null) System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000 else null,
            receiptDate = System.currentTimeMillis(),
            status = if (original.docType.isInvoice) DocumentStatus.DRAFT else DocumentStatus.PAID,
            amountPaid = if (original.docType.isInvoice) 0.0 else original.grandTotal,
            pdfFilePath = null
        )
        return saveDocument(copy)
    }

    suspend fun updateDocumentStatus(id: Long, newStatus: DocumentStatus, amountPaid: Double? = null) {
        val existing = documentDao.getDocumentByIdDirect(id) ?: return
        val updatedDomain = existing.toDomain().let { doc ->
            doc.copy(
                status = newStatus,
                amountPaid = amountPaid ?: if (newStatus == DocumentStatus.PAID) doc.grandTotal else doc.amountPaid
            )
        }
        documentDao.updateDocument(DocumentEntity.fromDomain(updatedDomain))
    }

    suspend fun saveBusinessProfile(profile: BusinessProfile) {
        val entity = BusinessProfileEntity(
            id = 1L,
            businessName = profile.businessName,
            tagline = profile.tagline,
            email = profile.email,
            phone = profile.phone,
            address = profile.address,
            website = profile.website,
            logoUri = profile.logoUri,
            taxId = profile.taxId,
            bankName = profile.bankName,
            accountNumber = profile.accountNumber,
            accountName = profile.accountName,
            defaultNotes = profile.defaultNotes,
            defaultTerms = profile.defaultTerms,
            defaultCurrency = profile.defaultCurrency
        )
        profileDao.insertOrUpdateProfile(entity)
    }

    suspend fun getBusinessProfileDirect(): BusinessProfile {
        val entity = profileDao.getProfileDirect()
        return entity?.let {
            BusinessProfile(
                id = it.id,
                businessName = it.businessName,
                tagline = it.tagline,
                email = it.email,
                phone = it.phone,
                address = it.address,
                website = it.website,
                logoUri = it.logoUri,
                taxId = it.taxId,
                bankName = it.bankName,
                accountNumber = it.accountNumber,
                accountName = it.accountName,
                defaultNotes = it.defaultNotes,
                defaultTerms = it.defaultTerms,
                defaultCurrency = it.defaultCurrency
            )
        } ?: defaultBusinessProfile()
    }

    suspend fun generateNextDocumentNumber(docType: DocumentType): String {
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val count = documentDao.countDocumentsByType(docType) + 1
        val prefix = docType.prefix
        return String.format(Locale.US, "%s-%s-%03d", prefix, currentYear, count)
    }

    private fun defaultBusinessProfile(): BusinessProfile = BusinessProfile(
        businessName = "Apex Solutions Nig.",
        tagline = "Professional Quality & Services",
        email = "billing@apexsolutions.ng",
        phone = "+234 801 234 5678",
        address = "Plot 12, Commercial Avenue, Victoria Island, Lagos",
        website = "www.apexsolutions.ng",
        logoUri = null,
        taxId = "TIN-10928374-001",
        bankName = "First Bank of Nigeria",
        accountNumber = "3012345678",
        accountName = "Apex Solutions Nig.",
        defaultNotes = "Thank you for your business. We appreciate your prompt payment.",
        defaultTerms = "Goods or services delivered in good condition. Payment due within 7 days of invoice issue date.",
        defaultCurrency = AppCurrency.DEFAULT.code
    )
}
