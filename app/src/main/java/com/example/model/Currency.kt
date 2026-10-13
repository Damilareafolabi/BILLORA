package com.example.model

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    NGN("NGN", "₦", "Nigerian Naira (NGN)"),
    USD("USD", "$", "US Dollar (USD)"),
    GBP("GBP", "£", "British Pound (GBP)"),
    EUR("EUR", "€", "Euro (EUR)"),
    GHS("GHS", "GH₵", "Ghanaian Cedi (GHS)"),
    KES("KES", "KSh", "Kenyan Shilling (KES)");

    companion object {
        val DEFAULT = NGN

        fun fromCode(code: String?): AppCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
