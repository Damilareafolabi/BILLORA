package com.example

import com.example.model.AppCurrency
import com.example.model.DocumentType
import com.example.model.InvoiceDocument
import com.example.model.LineItem
import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceCalculationUnitTest {

    @Test
    fun testLineItemTotalWithoutDiscount() {
        val item = LineItem(description = "Logo Design", quantity = 2.0, unitPrice = 15000.0)
        assertEquals(30000.0, item.totalAmount, 0.001)
    }

    @Test
    fun testLineItemTotalWithDiscount() {
        val item = LineItem(description = "Consultation", quantity = 3.0, unitPrice = 10000.0, discountPercent = 10.0)
        // 30000 - 10% = 27000
        assertEquals(27000.0, item.totalAmount, 0.001)
    }

    @Test
    fun testInvoiceSubtotalDiscountAndTaxCalculations() {
        val items = listOf(
            LineItem(description = "Item 1", quantity = 1.0, unitPrice = 50000.0),
            LineItem(description = "Item 2", quantity = 2.0, unitPrice = 25000.0)
        )
        // Subtotal = 100,000
        val doc = InvoiceDocument(
            items = items,
            discountPercent = 10.0, // 10% of 100,000 = 10,000 -> After discount = 90,000
            taxPercent = 7.5,       // 7.5% of 90,000 = 6,750 -> Grand Total = 96,750
            amountPaid = 50000.0    // Balance Due = 46,750
        )

        assertEquals(100000.0, doc.subtotal, 0.001)
        assertEquals(10000.0, doc.discountAmount, 0.001)
        assertEquals(90000.0, doc.afterDiscount, 0.001)
        assertEquals(6750.0, doc.taxAmount, 0.001)
        assertEquals(96750.0, doc.grandTotal, 0.001)
        assertEquals(46750.0, doc.balanceDue, 0.001)
    }

    @Test
    fun testDefaultCurrencyIsNigerianNaira() {
        assertEquals(AppCurrency.NGN, AppCurrency.DEFAULT)
        assertEquals("₦", AppCurrency.DEFAULT.symbol)
        assertEquals("NGN", AppCurrency.DEFAULT.code)
    }
}
