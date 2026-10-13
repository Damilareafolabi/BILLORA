package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.model.BusinessProfile
import com.example.model.DocumentStatus
import com.example.model.InvoiceDocument
import com.example.model.PaymentMethod
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f

    // Theme Colors
    private val COLOR_CRIMSON = Color.rgb(198, 40, 40)
    private val COLOR_CRIMSON_DARK = Color.rgb(183, 28, 28)
    private val COLOR_CHARCOAL = Color.rgb(30, 30, 36)
    private val COLOR_TEXT_MUTED = Color.rgb(100, 100, 110)
    private val COLOR_DIVIDER = Color.rgb(225, 225, 230)
    private val COLOR_TABLE_BG_ALT = Color.rgb(248, 248, 250)
    private val COLOR_SUCCESS = Color.rgb(16, 128, 64)

    fun generatePdf(
        context: Context,
        document: InvoiceDocument,
        profile: BusinessProfile
    ): File {
        val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val fileName = "${document.documentNumber.replace("[^a-zA-Z0-9-]".toRegex(), "_")}.pdf"
        val outputFile = File(pdfDir, fileName)

        val pdfDoc = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawDocumentPage(context, canvas, document, profile)

        pdfDoc.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return outputFile
    }

    private fun drawDocumentPage(
        context: Context,
        canvas: Canvas,
        doc: InvoiceDocument,
        profile: BusinessProfile
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var currentY = MARGIN

        // 1. Top Crimson Brand Accent Strip
        paint.color = COLOR_CRIMSON
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 12f, paint)

        currentY += 16f

        // 2. Header Area: Business Info & Logo (Left) vs Document Badge (Right)
        val startY = currentY

        // Draw Business Logo if available, or default app logo
        val logoBitmap = loadBusinessLogo(context, profile.logoUri)
        var leftTextX = MARGIN
        if (logoBitmap != null) {
            val logoSize = 48f
            val destRect = RectF(MARGIN, currentY, MARGIN + logoSize, currentY + logoSize)
            canvas.drawBitmap(logoBitmap, null, destRect, paint)
            leftTextX += logoSize + 12f
        }

        // Business Name
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = COLOR_CHARCOAL
        canvas.drawText(profile.businessName.ifBlank { "My Business" }, leftTextX, currentY + 14f, paint)

        // Business Tagline
        if (profile.tagline.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 8.5f
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText(profile.tagline, leftTextX, currentY + 26f, paint)
        }

        // Business Contact Lines
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = COLOR_TEXT_MUTED
        var contactY = currentY + 38f
        if (profile.address.isNotBlank()) {
            canvas.drawText(profile.address, leftTextX, contactY, paint)
            contactY += 11f
        }
        val phoneEmail = listOfNotNull(
            profile.phone.takeIf { it.isNotBlank() },
            profile.email.takeIf { it.isNotBlank() }
        ).joinToString(" • ")
        if (phoneEmail.isNotBlank()) {
            canvas.drawText(phoneEmail, leftTextX, contactY, paint)
            contactY += 11f
        }
        if (profile.taxId.isNotBlank()) {
            canvas.drawText("Tax/Reg ID: ${profile.taxId}", leftTextX, contactY, paint)
            contactY += 11f
        }

        // Right Header: Document Type Badge & Metadata
        val rightAlignX = PAGE_WIDTH - MARGIN
        paint.textAlign = Paint.Align.RIGHT

        // Document Type Title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = COLOR_CRIMSON
        val docTitle = if (doc.docType.isInvoice) "INVOICE" else "OFFICIAL RECEIPT"
        canvas.drawText(docTitle, rightAlignX, startY + 18f, paint)

        // Document Number
        paint.textSize = 11f
        paint.color = COLOR_CHARCOAL
        canvas.drawText("# ${doc.documentNumber}", rightAlignX, startY + 34f, paint)

        // Date & Status
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = COLOR_TEXT_MUTED

        val dateLabel = if (doc.docType.isInvoice) {
            "Date: ${doc.formattedIssueDate()}  |  Due: ${doc.formattedDueDate()}"
        } else {
            "Date: ${doc.formattedReceiptDate()}"
        }
        canvas.drawText(dateLabel, rightAlignX, startY + 48f, paint)

        // Status Badge Pill on Right
        val statusText = doc.status.label.uppercase(Locale.US)
        val statusBgColor = when (doc.status) {
            DocumentStatus.PAID -> COLOR_SUCCESS
            DocumentStatus.OVERDUE -> COLOR_CRIMSON_DARK
            DocumentStatus.SENT, DocumentStatus.PENDING -> Color.rgb(2, 132, 199)
            DocumentStatus.CANCELLED -> Color.rgb(107, 114, 128)
            DocumentStatus.DRAFT -> Color.rgb(75, 85, 99)
        }
        val pillWidth = 65f
        val pillHeight = 16f
        val pillRect = RectF(rightAlignX - pillWidth, startY + 54f, rightAlignX, startY + 54f + pillHeight)
        paint.textAlign = Paint.Align.LEFT
        paint.color = statusBgColor
        canvas.drawRoundRect(pillRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(statusText, pillRect.centerX(), pillRect.centerY() + 3f, paint)

        currentY = maxOf(contactY + 12f, startY + 76f)

        // 3. Divider Line
        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, paint)
        paint.style = Paint.Style.FILL

        currentY += 12f

        // 4. Customer Info Block & Receipt Payment Details Block (2-column layout)
        val customerBoxWidth = (PAGE_WIDTH - (MARGIN * 2) - 16f) / 2
        val col1X = MARGIN
        val col2X = MARGIN + customerBoxWidth + 16f

        // Box 1: Customer Details
        paint.color = Color.rgb(250, 250, 252)
        val boxRect1 = RectF(col1X, currentY, col1X + customerBoxWidth, currentY + 68f)
        canvas.drawRoundRect(boxRect1, 6f, 6f, paint)
        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(boxRect1, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        paint.color = COLOR_CRIMSON
        val clientLabel = if (doc.docType.isInvoice) "BILLED TO / CUSTOMER" else "RECEIVED FROM"
        canvas.drawText(clientLabel, col1X + 8f, currentY + 14f, paint)

        paint.textSize = 10f
        paint.color = COLOR_CHARCOAL
        canvas.drawText(doc.customerName.ifBlank { "Valued Customer" }, col1X + 8f, currentY + 28f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = COLOR_TEXT_MUTED
        var custY = currentY + 40f
        if (doc.customerPhone.isNotBlank()) {
            canvas.drawText("Tel: ${doc.customerPhone}", col1X + 8f, custY, paint)
            custY += 11f
        }
        if (doc.customerEmail.isNotBlank()) {
            canvas.drawText("Email: ${doc.customerEmail}", col1X + 8f, custY, paint)
            custY += 11f
        }
        if (doc.customerAddress.isNotBlank()) {
            val addr = if (doc.customerAddress.length > 35) doc.customerAddress.take(32) + "..." else doc.customerAddress
            canvas.drawText("Addr: $addr", col1X + 8f, custY, paint)
        }

        // Box 2: Payment / Reference Details (for Receipts or Bank Info for Invoices)
        val boxRect2 = RectF(col2X, currentY, col2X + customerBoxWidth, currentY + 68f)
        paint.color = Color.rgb(250, 250, 252)
        canvas.drawRoundRect(boxRect2, 6f, 6f, paint)
        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(boxRect2, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        paint.color = COLOR_CRIMSON
        val metaLabel = if (doc.docType.isReceipt) "PAYMENT & TRANSACTION" else "PAYMENT INSTRUCTIONS"
        canvas.drawText(metaLabel, col2X + 8f, currentY + 14f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = COLOR_CHARCOAL

        if (doc.docType.isReceipt) {
            canvas.drawText("Method: ${doc.paymentMethod?.label ?: "Bank Transfer"}", col2X + 8f, currentY + 28f, paint)
            if (doc.transactionReference.isNotBlank()) {
                canvas.drawText("Ref #: ${doc.transactionReference}", col2X + 8f, currentY + 40f, paint)
            }
            if (doc.receivedBy.isNotBlank()) {
                canvas.drawText("Cashier: ${doc.receivedBy}", col2X + 8f, currentY + 52f, paint)
            }
            canvas.drawText("Currency: ${doc.currency.displayName}", col2X + 8f, currentY + 63f, paint)
        } else {
            if (profile.bankName.isNotBlank() && profile.accountNumber.isNotBlank()) {
                canvas.drawText("Bank: ${profile.bankName}", col2X + 8f, currentY + 28f, paint)
                canvas.drawText("Acct No: ${profile.accountNumber}", col2X + 8f, currentY + 40f, paint)
                canvas.drawText("Acct Name: ${profile.accountName}", col2X + 8f, currentY + 52f, paint)
            } else {
                canvas.drawText("Currency: ${doc.currency.displayName}", col2X + 8f, currentY + 28f, paint)
                canvas.drawText("Payment Terms: Due in 7 days", col2X + 8f, currentY + 40f, paint)
            }
        }

        currentY += 80f

        // 5. Line Items Table
        val colDescX = MARGIN + 8f
        val colQtyX = MARGIN + 270f
        val colRateX = MARGIN + 330f
        val colDiscX = MARGIN + 420f
        val colTotalX = PAGE_WIDTH - MARGIN - 8f

        // Table Header Bar (Crimson / Charcoal)
        val tableHeaderHeight = 22f
        paint.color = COLOR_CHARCOAL
        val headerRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight)
        canvas.drawRoundRect(headerRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DESCRIPTION / ITEM", colDescX, currentY + 14f, paint)

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("QTY", colQtyX, currentY + 14f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("UNIT PRICE", colRateX, currentY + 14f, paint)
        canvas.drawText("DISC %", colDiscX, currentY + 14f, paint)
        canvas.drawText("TOTAL", colTotalX, currentY + 14f, paint)

        currentY += tableHeaderHeight + 4f

        // Table Rows
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f

        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        doc.items.forEachIndexed { index, item ->
            val rowHeight = 20f
            if (index % 2 == 1) {
                paint.color = COLOR_TABLE_BG_ALT
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, paint)
            }

            paint.color = COLOR_CHARCOAL
            paint.textAlign = Paint.Align.LEFT
            val desc = if (item.description.length > 40) item.description.take(38) + "..." else item.description.ifBlank { "Line item ${index + 1}" }
            canvas.drawText(desc, colDescX, currentY + 13f, paint)

            paint.textAlign = Paint.Align.CENTER
            val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
            canvas.drawText(qtyStr, colQtyX, currentY + 13f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${doc.currency.symbol} ${numberFormat.format(item.unitPrice)}", colRateX, currentY + 13f, paint)

            val discStr = if (item.discountPercent > 0) "${item.discountPercent}%" else "-"
            canvas.drawText(discStr, colDiscX, currentY + 13f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${doc.currency.symbol} ${numberFormat.format(item.totalAmount)}", colTotalX, currentY + 13f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            // Divider under row
            paint.color = COLOR_DIVIDER
            paint.strokeWidth = 0.5f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, paint)
            paint.style = Paint.Style.FILL

            currentY += rowHeight
        }

        currentY += 12f

        // 6. Summary Totals Box (Right Side) & Notes/Bank (Left Side)
        val summaryWidth = 200f
        val summaryX = PAGE_WIDTH - MARGIN - summaryWidth
        var summaryY = currentY

        // Subtotal
        drawSummaryRow(canvas, paint, "Subtotal", "${doc.currency.symbol} ${numberFormat.format(doc.subtotal)}", summaryX, summaryY, summaryWidth, false)
        summaryY += 15f

        // Overall Discount
        if (doc.discountPercent > 0) {
            drawSummaryRow(canvas, paint, "Discount (${doc.discountPercent}%)", "- ${doc.currency.symbol} ${numberFormat.format(doc.discountAmount)}", summaryX, summaryY, summaryWidth, false)
            summaryY += 15f
        }

        // Tax / VAT
        if (doc.taxPercent > 0) {
            drawSummaryRow(canvas, paint, "VAT / Tax (${doc.taxPercent}%)", "+ ${doc.currency.symbol} ${numberFormat.format(doc.taxAmount)}", summaryX, summaryY, summaryWidth, false)
            summaryY += 15f
        }

        // Grand Total Box
        paint.color = COLOR_CRIMSON
        val totalBoxRect = RectF(summaryX, summaryY, summaryX + summaryWidth, summaryY + 24f)
        canvas.drawRoundRect(totalBoxRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL", summaryX + 8f, summaryY + 16f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${doc.currency.symbol} ${numberFormat.format(doc.grandTotal)}", summaryX + summaryWidth - 8f, summaryY + 16f, paint)

        summaryY += 30f

        // Paid & Balance Due
        if (doc.docType.isReceipt || doc.amountPaid > 0) {
            drawSummaryRow(canvas, paint, "Amount Paid", "${doc.currency.symbol} ${numberFormat.format(doc.amountPaid)}", summaryX, summaryY, summaryWidth, false)
            summaryY += 15f

            val balanceColor = if (doc.balanceDue <= 0.01) COLOR_SUCCESS else COLOR_CRIMSON
            drawSummaryRow(canvas, paint, "Balance Due", "${doc.currency.symbol} ${numberFormat.format(doc.balanceDue)}", summaryX, summaryY, summaryWidth, true, balanceColor)
            summaryY += 18f
        }

        // Left Side: Notes & Terms & Conditions
        val notesWidth = summaryX - MARGIN - 24f
        var notesY = currentY

        if (doc.notes.isNotBlank() || profile.defaultNotes.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            paint.color = COLOR_CRIMSON
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("NOTES / REMARKS", MARGIN, notesY + 10f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            paint.color = COLOR_CHARCOAL
            val textNotes = doc.notes.ifBlank { profile.defaultNotes }
            notesY += 22f
            canvas.drawText(textNotes.take(120), MARGIN, notesY, paint)
            notesY += 16f
        }

        if (doc.terms.isNotBlank() || profile.defaultTerms.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            paint.color = COLOR_CRIMSON
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("TERMS & CONDITIONS", MARGIN, notesY + 10f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.5f
            paint.color = COLOR_TEXT_MUTED
            val textTerms = doc.terms.ifBlank { profile.defaultTerms }
            notesY += 22f
            canvas.drawText(textTerms.take(130), MARGIN, notesY, paint)
            notesY += 16f
        }

        // 7. Signature / Stamp Line (Bottom Area)
        val signatureY = PAGE_HEIGHT - MARGIN - 48f
        val sigLineStartX = PAGE_WIDTH - MARGIN - 160f
        val sigLineEndX = PAGE_WIDTH - MARGIN

        paint.color = COLOR_TEXT_MUTED
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        canvas.drawLine(sigLineStartX, signatureY, sigLineEndX, signatureY, paint)
        paint.pathEffect = null
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Authorized Signature & Stamp", (sigLineStartX + sigLineEndX) / 2f, signatureY + 12f, paint)

        // 8. Footer Accent & Watermark
        val footerY = PAGE_HEIGHT - MARGIN + 12f
        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 0.5f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(MARGIN, footerY - 14f, PAGE_WIDTH - MARGIN, footerY - 14f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 7f
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText("Generated by Billora — SimeonJr Studios (Offline Business Suite)", MARGIN, footerY, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page 1 of 1", PAGE_WIDTH - MARGIN, footerY, paint)
    }

    private fun drawSummaryRow(
        canvas: Canvas,
        paint: Paint,
        label: String,
        value: String,
        x: Float,
        y: Float,
        width: Float,
        isBold: Boolean,
        overrideColor: Int? = null
    ) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = overrideColor ?: COLOR_CHARCOAL

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(label, x + 4f, y + 10f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, x + width - 4f, y + 10f, paint)
    }

    private fun loadBusinessLogo(context: Context, logoUriString: String?): Bitmap? {
        if (!logoUriString.isNullOrBlank()) {
            try {
                val uri = Uri.parse(logoUriString)
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: "")
                    if (file.exists()) {
                        return BitmapFactory.decodeFile(file.absolutePath)
                    }
                } else if (uri.scheme == "content") {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        return BitmapFactory.decodeStream(stream)
                    }
                }
            } catch (_: Exception) {
                // fall back to app logo
            }
        }
        // Try to load bundled app logo
        return try {
            val logoFile = File(context.filesDir, "logo.png")
            if (logoFile.exists()) {
                BitmapFactory.decodeFile(logoFile.absolutePath)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
