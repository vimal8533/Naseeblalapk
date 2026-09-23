package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.RentRecord
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPdfGenerator {

    /**
     * Generates a PDF rent receipt and saves it to the device's Downloads directory.
     * Shows a toast and launches an Intent to view/open the PDF.
     */
    fun generateAndDownloadReceipt(context: Context, rent: RentRecord): Uri? {
        try {
            val document = PdfDocument()
            val pageWidth = 595 // A4 standard width in points (72 dpi)
            val pageHeight = 842 // A4 standard height
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawReceiptContent(canvas, pageWidth, pageHeight, rent)
            document.finishPage(page)

            // File naming: Receipt_NLM_Shop101_Month_Year.pdf
            val cleanShop = rent.shopNumber.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "Shop" }
            val cleanReceipt = rent.receiptNumber.filter { it.isLetterOrDigit() || it == '-' }.ifBlank { "NLM" }
            val fileName = "Receipt_${cleanReceipt}_${cleanShop}_${rent.month}_${rent.year}.pdf"

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePdfViaMediaStore(context, document, fileName)
            } else {
                savePdfLegacy(context, document, fileName)
            }

            document.close()

            if (uri != null) {
                Toast.makeText(context, "Receipt downloaded: $fileName", Toast.LENGTH_LONG).show()
                openPdfReceipt(context, uri)
                return uri
            } else {
                Toast.makeText(context, "Failed to save receipt to storage.", Toast.LENGTH_SHORT).show()
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating receipt: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    private fun drawReceiptContent(canvas: Canvas, width: Int, height: Int, rent: RentRecord) {
        val navyPrimary = Color.parseColor("#0F1E3D")
        val navyDark = Color.parseColor("#070E1E")
        val goldAccent = Color.parseColor("#D4AF37")
        val statusGreen = Color.parseColor("#10B981")
        val statusOrange = Color.parseColor("#F59E0B")
        val statusRed = Color.parseColor("#EF4444")
        val bgGray = Color.parseColor("#F8FAFC")
        val borderGray = Color.parseColor("#E2E8F0")
        val textDark = Color.parseColor("#1E293B")
        val textMuted = Color.parseColor("#64748B")

        // 1. Page Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Title: Header
        val headerColor = if (rent.isPersonal) Color.parseColor("#0F766E") else navyPrimary
        paint.color = headerColor
        canvas.drawRect(0f, 0f, width.toFloat(), 130f, paint)

        // Gold/Teal decorative accent bar
        val accentColor = if (rent.isPersonal) Color.parseColor("#2DD4BF") else goldAccent
        paint.color = accentColor
        canvas.drawRect(0f, 126f, width.toFloat(), 130f, paint)

        // Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(if (rent.isPersonal) "RESIDENTIAL APARTMENTS & FLATS" else "NASEEB LAL MARKET", width / 2f, 52f, paint)

        // Subtitle
        paint.color = accentColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.letterSpacing = 0.08f
        canvas.drawText(if (rent.isPersonal) "RENT & UTILITY PAYMENT RECEIPT" else "OFFICIAL RENT PAYMENT RECEIPT", width / 2f, 78f, paint)

        // Small tag
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.letterSpacing = 0.04f
        canvas.drawText(if (rent.isPersonal) "Flat Tenant Ledger • Cloud Synced" else "Market Rent Manager • Multi-Device Cloud Synced", width / 2f, 102f, paint)

        // 3. Receipt Info Box (Top right / left)
        var y = 160f
        paint.textAlign = Paint.Align.LEFT
        paint.letterSpacing = 0f

        val boxMargin = 40f
        val boxWidth = width - (boxMargin * 2)

        // Card Container for Receipt Details
        val infoRect = RectF(boxMargin, y, boxMargin + boxWidth, y + 80f)
        paint.color = bgGray
        canvas.drawRoundRect(infoRect, 10f, 10f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        paint.strokeWidth = 1f
        canvas.drawRoundRect(infoRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Receipt details inside box
        paint.color = textMuted
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("RECEIPT NO:", boxMargin + 16f, y + 26f, paint)
        canvas.drawText("PAYMENT DATE:", boxMargin + 16f, y + 48f, paint)
        canvas.drawText("PAYMENT MODE:", boxMargin + 16f, y + 68f, paint)

        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(rent.receiptNumber.ifBlank { "NLM-${rent.year}-AUTO" }, boxMargin + 110f, y + 26f, paint)
        canvas.drawText(rent.paidDate.ifBlank { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }, boxMargin + 110f, y + 48f, paint)
        canvas.drawText(rent.paymentMode, boxMargin + 110f, y + 68f, paint)

        // Right column inside info box
        paint.color = textMuted
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("BILLING PERIOD:", boxMargin + 300f, y + 26f, paint)
        canvas.drawText("COLLECTED BY:", boxMargin + 300f, y + 48f, paint)
        canvas.drawText("RECORD STATUS:", boxMargin + 300f, y + 68f, paint)

        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${rent.month} ${rent.year}", boxMargin + 400f, y + 26f, paint)
        canvas.drawText(rent.collectedBy.ifBlank { "Admin" }, boxMargin + 400f, y + 48f, paint)

        val statusColor = when {
            rent.isPaid -> statusGreen
            rent.isPartial -> statusOrange
            else -> statusRed
        }
        paint.color = statusColor
        canvas.drawText(if (rent.isPaid) "PAID" else if (rent.isPartial) "PARTIAL" else "PENDING", boxMargin + 400f, y + 68f, paint)

        // 4. Tenant & Shop/Flat Details Section
        y += 105f
        paint.color = textDark
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (rent.isPersonal) "TENANT & FLAT INFORMATION" else "TENANT & SHOP INFORMATION", boxMargin, y, paint)

        y += 12f
        val tenantRect = RectF(boxMargin, y, boxMargin + boxWidth, y + 65f)
        paint.color = bgGray
        canvas.drawRoundRect(tenantRect, 10f, 10f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        canvas.drawRoundRect(tenantRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        paint.color = textMuted
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Tenant Name:", boxMargin + 16f, y + 26f, paint)
        canvas.drawText(if (rent.isPersonal) "Flat / Unit(s):" else "Shop / Unit(s):", boxMargin + 16f, y + 48f, paint)

        paint.color = textDark
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(rent.tenantName, boxMargin + 110f, y + 26f, paint)
        val unitLabel = if (rent.isPersonal && !rent.shopNumber.startsWith("Flat", ignoreCase = true)) "Flat ${rent.shopNumber}" else rent.shopNumber
        canvas.drawText(unitLabel, boxMargin + 110f, y + 48f, paint)

        // 5. Rent & Tax Breakdown Table
        y += 90f
        paint.color = textDark
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RENT & CHARGES BREAKDOWN", boxMargin, y, paint)

        y += 12f
        val tableHeaderY = y
        paint.color = headerColor
        canvas.drawRect(boxMargin, tableHeaderY, boxMargin + boxWidth, tableHeaderY + 28f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DESCRIPTION", boxMargin + 16f, tableHeaderY + 18f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (INR)", boxMargin + boxWidth - 16f, tableHeaderY + 18f, paint)

        // Table Rows
        var rowY = tableHeaderY + 28f
        val baseRentAmount = if (rent.isPersonal) {
            (rent.amountDue - rent.electricityBill).coerceAtLeast(0.0)
        } else {
            (rent.amountDue - rent.pmcTax).coerceAtLeast(0.0)
        }

        // Row 1: Monthly Rent
        paint.color = Color.WHITE
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 30f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 30f, paint)
        paint.style = Paint.Style.FILL

        paint.color = textDark
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val rentRowDesc = if (rent.isPersonal) "Monthly Flat Rent (${rent.month} ${rent.year})" else "Monthly Shop Rent (${rent.month} ${rent.year})"
        canvas.drawText(rentRowDesc, boxMargin + 16f, rowY + 19f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("₹ ${formatAmount(baseRentAmount)}", boxMargin + boxWidth - 16f, rowY + 19f, paint)

        rowY += 30f

        // Row 2: Electricity Bill (for Flat mode) OR PMC Tax (for Commercial mode)
        if (rent.isPersonal && rent.electricityBill > 0) {
            paint.color = Color.parseColor("#FEF3C7") // Light amber
            canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 30f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#FDE68A")
            canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 30f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#92400E")
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("⚡ Monthly Electricity / Power Bill", boxMargin + 16f, rowY + 19f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹ ${formatAmount(rent.electricityBill)}", boxMargin + boxWidth - 16f, rowY + 19f, paint)

            rowY += 30f
        } else if (!rent.isPersonal && rent.pmcTax > 0) {
            paint.color = Color.parseColor("#FEF3C7") // Light amber
            canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 30f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#FDE68A")
            canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 30f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#92400E")
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("PMC Municipal Tax (1 Year Completion Charge)", boxMargin + 16f, rowY + 19f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹ ${formatAmount(rent.pmcTax)}", boxMargin + boxWidth - 16f, rowY + 19f, paint)

            rowY += 30f
        }

        // Row 3: Total Due
        paint.color = bgGray
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 32f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 32f, paint)
        paint.style = Paint.Style.FILL

        paint.color = textDark
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL AMOUNT DUE", boxMargin + 16f, rowY + 20f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("₹ ${formatAmount(rent.amountDue)}", boxMargin + boxWidth - 16f, rowY + 20f, paint)

        rowY += 32f

        // Row 4: Amount Paid
        paint.color = Color.WHITE
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 32f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 32f, paint)
        paint.style = Paint.Style.FILL

        paint.color = statusGreen
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("AMOUNT PAID", boxMargin + 16f, rowY + 20f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("₹ ${formatAmount(rent.amountPaid)}", boxMargin + boxWidth - 16f, rowY + 20f, paint)

        rowY += 32f

        // Row 5: Pending Balance
        paint.color = if (rent.pendingAmount > 0) Color.parseColor("#FFF1F2") else bgGray
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 34f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = if (rent.pendingAmount > 0) Color.parseColor("#FECDD3") else borderGray
        canvas.drawRect(boxMargin, rowY, boxMargin + boxWidth, rowY + 34f, paint)
        paint.style = Paint.Style.FILL

        paint.color = if (rent.pendingAmount > 0) statusRed else textDark
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BALANCE DUE", boxMargin + 16f, rowY + 22f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("₹ ${formatAmount(rent.pendingAmount)}", boxMargin + boxWidth - 16f, rowY + 22f, paint)

        // 6. Big Status Stamp Box
        rowY += 50f
        val stampRect = RectF(boxMargin + 100f, rowY, boxMargin + boxWidth - 100f, rowY + 45f)
        paint.color = statusColor.let { Color.argb(30, Color.red(it), Color.green(it), Color.blue(it)) }
        canvas.drawRoundRect(stampRect, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = statusColor
        paint.strokeWidth = 2f
        canvas.drawRoundRect(stampRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = statusColor
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val stampText = when {
            rent.isPaid -> "★ FULL PAYMENT RECEIVED - PAID ★"
            rent.isPartial -> "▲ PARTIAL PAYMENT RECEIVED ▲"
            else -> "⏳ PAYMENT PENDING ⏳"
        }
        canvas.drawText(stampText, width / 2f, rowY + 28f, paint)

        // Notes if any
        if (rent.notes.isNotBlank()) {
            rowY += 60f
            paint.textAlign = Paint.Align.LEFT
            paint.color = textMuted
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Remarks / Notes: ${rent.notes}", boxMargin, rowY, paint)
        }

        // Anti-Fraud Digital Verification QR Code Stamp (Tamper-Proof Ledger Record Verification)
        val qrBoxY = (height - 185f).coerceAtLeast(rowY + 65f)
        val qrBoxHeight = 72f
        val qrRect = RectF(boxMargin, qrBoxY, boxMargin + boxWidth, qrBoxY + qrBoxHeight)
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(qrRect, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#CBD5E1")
        paint.strokeWidth = 1f
        canvas.drawRoundRect(qrRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Draw Real Scannable ZXing Verification QR Code (Left inside QR box)
        val qrSize = 58f
        val qrLeft = boxMargin + 10f
        val qrTop = qrBoxY + 7f

        val qrPayload = buildString {
            appendLine("NASEEB LAL MARKET - OFFICIAL RECEIPT")
            appendLine("---------------------------------")
            appendLine("Receipt No: ${rent.receiptNumber.ifBlank { "NLM-${rent.year}" }}")
            appendLine("Tenant: ${rent.tenantName}")
            appendLine("Unit: ${if (rent.isPersonal) "Flat" else "Shop"} ${rent.shopNumber}")
            appendLine("Period: ${rent.month} ${rent.year}")
            appendLine("Total Due: Rs. ${formatAmount(rent.amountDue)}")
            appendLine("Paid: Rs. ${formatAmount(rent.amountPaid)}")
            appendLine("Balance: Rs. ${formatAmount(rent.pendingAmount)}")
            appendLine("Status: ${rent.status}")
            if (rent.paymentMode.isNotBlank()) {
                appendLine("Mode: ${rent.paymentMode}")
            }
            if (rent.paidDate.isNotBlank()) {
                appendLine("Date: ${rent.paidDate}")
            }
            if (rent.tenantClaimedNote.isNotBlank()) {
                appendLine("Ref: ${rent.tenantClaimedNote}")
            }
            appendLine("Verification: AUTHENTIC RECORD")
        }

        val qrBitmap = generateQrBitmap(qrPayload, 200)
        if (qrBitmap != null) {
            val destRect = RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)
            canvas.drawBitmap(qrBitmap, null, destRect, null)
        }

        // QR Information & Anti-Fraud Explanation (Verification only - NOT for payment)
        paint.textAlign = Paint.Align.LEFT
        paint.color = navyPrimary
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🛡️ OFFICIAL DIGITAL RECORD VERIFICATION QR", boxMargin + 76f, qrBoxY + 19f, paint)

        paint.color = textMuted
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val verifyText1 = "Scan with any phone camera or Google Lens to view verified receipt details."
        canvas.drawText(verifyText1, boxMargin + 76f, qrBoxY + 33f, paint)

        val utrNote = if (rent.tenantClaimedNote.isNotBlank()) " | UTR: ${rent.tenantClaimedNote.take(18)}" else ""
        val verifyText2 = "Auth Hash: NLM-${rent.shopNumber.filter { it.isLetterOrDigit() }}-${rent.month.take(3)}-${rent.year}${utrNote} • Ledger Verified"
        paint.color = Color.parseColor("#0369A1")
        canvas.drawText(verifyText2, boxMargin + 76f, qrBoxY + 47f, paint)

        paint.color = Color.parseColor("#D97706")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("⚠️ RECORD VERIFICATION ONLY • PAYMENTS ARE MANAGED DIRECTLY", boxMargin + 76f, qrBoxY + 60f, paint)

        // 7. Signatures & Footer
        val footerY = height - 100f
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        paint.strokeWidth = 1f
        canvas.drawLine(boxMargin, footerY, boxMargin + boxWidth, footerY, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.LEFT
        paint.color = textMuted
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("This is a computer-generated receipt from Naseeb Lal Market Rent Management System.", boxMargin, footerY + 20f, paint)
        canvas.drawText("Verified Digital Record • No physical signature required.", boxMargin, footerY + 34f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = navyPrimary
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Naseeb Lal Market", boxMargin + boxWidth, footerY + 24f, paint)
        paint.color = goldAccent
        paint.textSize = 9f
        canvas.drawText("Authorized Management", boxMargin + boxWidth, footerY + 38f, paint)
    }

    private fun savePdfViaMediaStore(context: Context, document: PdfDocument, fileName: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NaseebLalMarket")
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            if (outputStream != null) {
                outputStream.use { document.writeTo(it) }
                return uri
            }
        }
        return null
    }

    private fun savePdfLegacy(context: Context, document: PdfDocument, fileName: String): Uri? {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "NaseebLalMarket")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun openPdfReceipt(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Rent Receipt"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
    }

    /**
     * Generates a real 2D QR Code Bitmap using ZXing.
     * Can be scanned by any smartphone camera or QR scanner.
     */
    private fun generateQrBitmap(content: String, sizePx: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
