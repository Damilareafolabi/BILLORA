package com.example.model

import java.util.UUID

data class LineItem(
    val id: String = UUID.randomUUID().toString(),
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val discountPercent: Double = 0.0
) {
    val totalAmount: Double
        get() {
            val base = quantity * unitPrice
            val discount = base * (discountPercent.coerceIn(0.0, 100.0) / 100.0)
            return (base - discount).coerceAtLeast(0.0)
        }
}

data class BusinessProfile(
    val id: Long = 1L,
    val businessName: String = "My Business",
    val tagline: String = "Quality & Excellence",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val website: String = "",
    val logoUri: String? = null,
    val taxId: String = "", // e.g. TIN / RC / VAT
    val bankName: String = "",
    val accountNumber: String = "",
    val accountName: String = "",
    val defaultNotes: String = "Thank you for your business!",
    val defaultTerms: String = "Payment is due within 7 days of invoice date.",
    val defaultCurrency: String = AppCurrency.DEFAULT.code
)
