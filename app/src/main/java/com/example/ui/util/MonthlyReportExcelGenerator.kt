package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.RentRecord
import com.example.model.Shop
import com.example.model.Tenant
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MonthlyReportExcelGenerator {

    /**
     * Generates a comprehensive, professional monthly Excel-compatible CSV report.
     * Includes "Shop Name (Dukan Ka Naam)" section along with full rent details.
     * Saves to Downloads and opens system share/view chooser (Excel, Google Sheets, WhatsApp).
     */
    fun exportAndShareMonthlyReport(
        context: Context,
        month: String,
        year: Int,
        rents: List<RentRecord>,
        shops: List<Shop>,
        tenants: List<Tenant>
    ): Uri? {
        try {
            val fileName = "Monthly_Rent_Report_${month}_${year}.csv"
            val csvContent = buildCsvContent(month, year, rents, shops, tenants)

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveCsvViaMediaStore(context, csvContent, fileName)
            } else {
                saveCsvLegacy(context, csvContent, fileName)
            }

            if (uri != null) {
                Toast.makeText(context, "✅ Excel Report Downloaded: $fileName", Toast.LENGTH_LONG).show()
                openAndShareExcelFile(context, uri, fileName, month, year)
                return uri
            } else {
                Toast.makeText(context, "Failed to save Excel file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun buildCsvContent(
        month: String,
        year: Int,
        rents: List<RentRecord>,
        shops: List<Shop>,
        tenants: List<Tenant>
    ): String {
        return buildString {
            // UTF-8 BOM for Microsoft Excel compatibility (avoids encoding issues)
            append('\uFEFF')

            // 1. Title & Header Summary Block
            appendLine("NASEEB LAL MARKET - MONTHLY RENT COLLECTION REPORT")
            appendLine("Report Month:,$month $year")
            appendLine("Generated On:,${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}")

            val totalDue = rents.sumOf { it.amountDue }
            val totalPaid = rents.sumOf { it.amountPaid }
            val totalPending = rents.sumOf { it.pendingAmount }
            val paidCount = rents.count { it.isPaid }
            val partialCount = rents.count { it.isPartial }
            val pendingCount = rents.count { it.isPending }

            appendLine("Total Shops / Records:,${rents.size}")
            appendLine("Total Rent Expected (Rs):,${String.format(Locale.US, "%.2f", totalDue)}")
            appendLine("Total Rent Collected (Rs):,${String.format(Locale.US, "%.2f", totalPaid)}")
            appendLine("Total Balance Pending (Rs):,${String.format(Locale.US, "%.2f", totalPending)}")
            appendLine("Status Summary:,Paid: $paidCount | Partial: $partialCount | Pending: $pendingCount")
            appendLine() // Blank line before data table

            // 2. Table Column Headers (Includes "Shop Name (Dukan Ka Naam)")
            val headers = listOf(
                "S.No.",
                "Shop No.",
                "Shop Name (Dukan Ka Naam)",
                "Tenant Name",
                "Mobile No.",
                "Base Rent (Rs)",
                "PMC Tax (Rs)",
                "Total Due (Rs)",
                "Amount Paid (Rs)",
                "Pending Due (Rs)",
                "Payment Status",
                "Payment Mode",
                "Payment Date",
                "Receipt No.",
                "Collected By",
                "Notes"
            )
            appendLine(headers.joinToString(",") { escapeCsv(it) })

            // 3. Data Rows
            val sortedRents = rents.sortedBy { it.shopNumber }
            for ((index, rent) in sortedRents.withIndex()) {
                val tenant = tenants.find { it.id == rent.tenantId }
                val shop = shops.find { it.id == rent.shopId || it.shopNumber == rent.shopNumber }

                // Shop Name / Dukan Ka Naam
                val shopName = tenant?.businessName?.trim()?.ifBlank {
                    shop?.notes?.trim()?.ifBlank { "" }
                } ?: ""

                val tenantPhone = tenant?.phone?.trim() ?: ""
                val baseRent = (rent.amountDue - rent.pmcTax).coerceAtLeast(0.0)

                val row = listOf(
                    (index + 1).toString(),
                    rent.shopNumber,
                    shopName.ifBlank { "-" },
                    rent.tenantName.ifBlank { "-" },
                    tenantPhone.ifBlank { "-" },
                    String.format(Locale.US, "%.2f", baseRent),
                    String.format(Locale.US, "%.2f", rent.pmcTax),
                    String.format(Locale.US, "%.2f", rent.amountDue),
                    String.format(Locale.US, "%.2f", rent.amountPaid),
                    String.format(Locale.US, "%.2f", rent.pendingAmount),
                    rent.status.uppercase(),
                    if (rent.amountPaid > 0) rent.paymentMode else "-",
                    rent.paidDate.ifBlank { "-" },
                    rent.receiptNumber.ifBlank { "-" },
                    rent.collectedBy.ifBlank { "-" },
                    rent.notes.ifBlank { "-" }
                )
                appendLine(row.joinToString(",") { escapeCsv(it) })
            }

            // 4. Grand Total Summary Row at bottom
            appendLine()
            val totalBaseRent = rents.sumOf { (it.amountDue - it.pmcTax).coerceAtLeast(0.0) }
            val totalPmcTax = rents.sumOf { it.pmcTax }

            val totalRow = listOf(
                "TOTAL",
                "${rents.size} Shops",
                "-",
                "-",
                "-",
                String.format(Locale.US, "%.2f", totalBaseRent),
                String.format(Locale.US, "%.2f", totalPmcTax),
                String.format(Locale.US, "%.2f", totalDue),
                String.format(Locale.US, "%.2f", totalPaid),
                String.format(Locale.US, "%.2f", totalPending),
                "-",
                "-",
                "-",
                "-",
                "-",
                "-"
            )
            appendLine(totalRow.joinToString(",") { escapeCsv(it) })
        }
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        return if (needsQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun saveCsvViaMediaStore(context: Context, content: String, fileName: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NaseebLalMarket")
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            if (outputStream != null) {
                outputStream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                return uri
            }
        }
        return null
    }

    private fun saveCsvLegacy(context: Context, content: String, fileName: String): Uri? {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "NaseebLalMarket")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { it.write(content.toByteArray(Charsets.UTF_8)) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun openAndShareExcelFile(
        context: Context,
        uri: Uri,
        fileName: String,
        month: String,
        year: Int
    ) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Naseeb Lal Market - Monthly Report $month $year")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📊 Naseeb Lal Market Monthly Rent Report for $month $year is attached.\nIncludes Shop Name, Tenant details, rent collected and dues."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Open / Share Monthly Excel Report ($fileName)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
