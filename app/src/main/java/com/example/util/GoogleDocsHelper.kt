package com.example.util

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper to create and export Naseeb Lal Market rent reports, tenant agreements,
 * and summaries directly to Google Docs via Google Docs REST API.
 */
object GoogleDocsHelper {

    private const val DOCS_API_BASE = "https://docs.googleapis.com/v1/documents"

    /**
     * Builds structured report content for a given month and year.
     */
    fun buildMonthlyReportDocumentContent(
        month: String,
        year: Int,
        rents: List<RentRecord>,
        shops: List<Shop>,
        tenants: List<Tenant>,
        isPersonalWorkspace: Boolean = false
    ): String {
        val totalExpected = rents.sumOf { it.amountDue }
        val totalCollected = rents.sumOf { it.amountPaid }
        val totalPending = rents.sumOf { it.pendingAmount }
        val paidCount = rents.count { it.isPaid }
        val pendingCount = rents.count { it.isPending || it.isPartial }
        val totalUnits = shops.size
        val occupiedCount = shops.count { it.isOccupied }
        val nowStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())

        val title = if (isPersonalWorkspace) {
            "RESIDENTIAL / PERSONAL PROPERTY RENT REPORT"
        } else {
            "NASEEB LAL MARKET - MONTHLY RENT REPORT"
        }

        return buildString {
            appendLine("================================================================")
            appendLine("           $title")
            appendLine("================================================================")
            appendLine("Report Period   : $month $year")
            appendLine("Generated On    : $nowStr")
            appendLine("Total Units     : $totalUnits (Occupied: $occupiedCount, Vacant: ${totalUnits - occupiedCount})")
            appendLine("Total Expected  : ₹${formatAmount(totalExpected)}")
            appendLine("Total Collected : ₹${formatAmount(totalCollected)} ($paidCount paid)")
            appendLine("Total Pending   : ₹${formatAmount(totalPending)} ($pendingCount pending/partial)")
            appendLine("================================================================")
            appendLine()
            appendLine("----------------------------------------------------------------")
            appendLine(String.format("%-10s | %-22s | %-12s | %-12s | %-10s", "Unit", "Tenant / Business", "Due (₹)", "Paid (₹)", "Status"))
            appendLine("----------------------------------------------------------------")

            rents.forEach { rent ->
                val tenant = tenants.firstOrNull { it.id == rent.tenantId }
                val displayTitle = if (tenant?.businessName?.isNotBlank() == true) tenant.businessName else (tenant?.name ?: rent.tenantName)
                val cleanTitle = if (displayTitle.length > 20) displayTitle.take(19) + "…" else displayTitle
                val statusStr = when {
                    rent.isPaid -> "PAID"
                    rent.isPartial -> "PARTIAL"
                    else -> "DUE"
                }
                val unitLabel = if (rent.isPersonal) "Flat ${rent.shopNumber}" else "Shop ${rent.shopNumber}"

                appendLine(
                    String.format(
                        "%-10s | %-22s | %-12s | %-12s | %-10s",
                        unitLabel.take(10),
                        cleanTitle,
                        formatAmount(rent.amountDue),
                        formatAmount(rent.amountPaid),
                        statusStr
                    )
                )
                if (rent.isPersonal && rent.electricityBill > 0) {
                    appendLine("           -> Rent: ₹${formatAmount(rent.amountDue - rent.electricityBill)} | Light Bill: ₹${formatAmount(rent.electricityBill)}")
                } else if (!rent.isPersonal && rent.pmcTax > 0) {
                    appendLine("           -> Rent: ₹${formatAmount(rent.amountDue - rent.pmcTax)} | PMC Tax: ₹${formatAmount(rent.pmcTax)}")
                }
                if (rent.paidDate.isNotBlank()) {
                    appendLine("           -> Paid Date: ${rent.paidDate} via ${rent.paymentMode} (Rcpt: ${rent.receiptNumber})")
                }
            }

            appendLine("----------------------------------------------------------------")
            appendLine()
            appendLine("================================================================")
            appendLine("Verified Digital Record • Naseeb Lal Market Management")
            appendLine("================================================================")
        }
    }

    /**
     * Builds structured tenant agreement / ledger document text.
     */
    fun buildTenantAgreementDocumentContent(
        tenant: Tenant,
        rents: List<RentRecord>
    ): String {
        val nowStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
        return buildString {
            appendLine("================================================================")
            appendLine("         TENANT RENT RECORD & AGREEMENT SUMMARY")
            appendLine("                 NASEEB LAL MARKET")
            appendLine("================================================================")
            appendLine("Generated On        : $nowStr")
            appendLine("Tenant Name         : ${tenant.name}")
            if (tenant.businessName.isNotBlank()) {
                appendLine("Business / Shop Name: ${tenant.businessName}")
            }
            appendLine("Contact Phone       : ${tenant.phone}")
            val unitWord = if (tenant.isPersonal) "Flat" else "Shop"
            appendLine("Assigned $unitWord(s)   : ${tenant.shopNumber} (${tenant.shopCount} units)")
            appendLine("Monthly Rent        : ₹${formatAmount(tenant.monthlyRent)}")
            appendLine("Security Deposit    : ₹${formatAmount(tenant.advanceDeposit)}")
            appendLine("Billing Cycle       : ${tenant.billingCycleDisplay}")
            appendLine("Joining Date        : ${tenant.joiningDate.ifBlank { "Active" }}")
            if (tenant.idProof.isNotBlank()) {
                appendLine("Govt ID Proof       : ${tenant.idProof}")
            }
            if (tenant.isPersonal) {
                appendLine("Monthly Electricity : ₹${formatAmount(tenant.electricityBill)}")
            } else {
                appendLine("Annual PMC Tax      : ₹${formatAmount(tenant.annualPmcTaxAmount)}")
            }
            appendLine("================================================================")
            appendLine()
            appendLine("RECENT PAYMENT RECORDS:")
            appendLine("----------------------------------------------------------------")
            val tenantRents = rents.filter { it.tenantId == tenant.id }.sortedByDescending { "${it.year}_${it.month}" }
            if (tenantRents.isEmpty()) {
                appendLine("No past rent records available.")
            } else {
                tenantRents.forEach { r ->
                    appendLine("• ${r.month} ${r.year} : Due ₹${formatAmount(r.amountDue)} | Paid ₹${formatAmount(r.amountPaid)} | Pending ₹${formatAmount(r.pendingAmount)} [${r.status}]")
                }
            }
            appendLine("----------------------------------------------------------------")
            appendLine("End of Tenant Summary")
        }
    }

    /**
     * Creates a new Google Doc using the Google Docs REST API.
     * @param authToken Valid OAuth 2.0 Bearer access token
     * @param title Title of the document
     * @param content Text body to write into the document
     * @return Pair<documentId, documentUrl> on success
     */
    suspend fun createGoogleDoc(
        authToken: String,
        title: String,
        content: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Create empty document with title
            val createUrl = URL(DOCS_API_BASE)
            val createConn = (createUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $authToken")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val createBody = JSONObject().apply {
                put("title", title)
            }

            OutputStreamWriter(createConn.outputStream, Charsets.UTF_8).use {
                it.write(createBody.toString())
            }

            val createResponseCode = createConn.responseCode
            if (createResponseCode !in 200..299) {
                val errReader = BufferedReader(InputStreamReader(createConn.errorStream ?: createConn.inputStream))
                val rawErr = errReader.readText()
                val friendlyMessage = parseFriendlyApiError(createResponseCode, rawErr)
                return@withContext Result.failure(Exception(friendlyMessage))
            }

            val responseText = BufferedReader(InputStreamReader(createConn.inputStream)).readText()
            val jsonResponse = JSONObject(responseText)
            val documentId = jsonResponse.getString("documentId")
            val docUrl = "https://docs.google.com/document/d/$documentId/edit"

            // Step 2: BatchUpdate to insert the text body into the document at index 1
            if (content.isNotBlank()) {
                val updateUrl = URL("$DOCS_API_BASE/$documentId:batchUpdate")
                val updateConn = (updateUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $authToken")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val requestsArray = JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put(
                                "insertText",
                                JSONObject().apply {
                                    put(
                                        "location",
                                        JSONObject().apply { put("index", 1) }
                                    )
                                    put("text", content)
                                }
                            )
                        }
                    )
                }

                val updateBody = JSONObject().apply {
                    put("requests", requestsArray)
                }

                OutputStreamWriter(updateConn.outputStream, Charsets.UTF_8).use {
                    it.write(updateBody.toString())
                }

                val updateCode = updateConn.responseCode
                if (updateCode !in 200..299) {
                    val updateErr = BufferedReader(InputStreamReader(updateConn.errorStream ?: updateConn.inputStream)).readText()
                    // Even if update failed, doc is created
                    return@withContext Result.success(Pair(documentId, docUrl))
                }
            }

            Result.success(Pair(documentId, docUrl))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Opens the created Google Doc in browser or Google Docs app.
     */
    fun openGoogleDocInBrowser(context: Context, docUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(docUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Downloads and saves document text locally as a .txt / .doc file in Downloads folder.
     */
    fun downloadDocumentAsFile(
        context: Context,
        fileName: String,
        content: String
    ): Uri? {
        val safeFileName = if (fileName.endsWith(".txt") || fileName.endsWith(".doc")) {
            fileName
        } else {
            "$fileName.doc"
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, safeFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/msword")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/NaseebLalMarket")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(uri)
                    if (outputStream != null) {
                        outputStream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                        Toast.makeText(context, "Saved to Downloads: $safeFileName", Toast.LENGTH_LONG).show()
                        openDownloadedFile(context, uri, safeFileName)
                        return uri
                    }
                }
                null
            } else {
                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "NaseebLalMarket")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, safeFileName)
                FileOutputStream(file).use { it.write(content.toByteArray(Charsets.UTF_8)) }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                Toast.makeText(context, "Saved file: $safeFileName", Toast.LENGTH_LONG).show()
                openDownloadedFile(context, uri, safeFileName)
                uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun openDownloadedFile(context: Context, uri: Uri, fileName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/msword")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Open in Google Docs / Office ($fileName)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Fallback plain view
        }
    }

    private fun parseFriendlyApiError(statusCode: Int, rawResponse: String): String {
        return try {
            val json = JSONObject(rawResponse)
            val errorObj = json.optJSONObject("error")
            val message = errorObj?.optString("message") ?: rawResponse
            val status = errorObj?.optString("status") ?: ""

            when {
                statusCode == 401 || status.contains("UNAUTHENTICATED", ignoreCase = true) ->
                    "Authentication expired or invalid. Please check your OAuth token or use 'Share to Docs' to open directly."
                statusCode == 403 && (status.contains("PERMISSION_DENIED", ignoreCase = true) || message.contains("Documents API has not been used", ignoreCase = true)) ->
                    "Google Docs API permission error: Ensure Google Docs API is enabled in your Google Cloud project (Project 29971594227)."
                statusCode == 429 || status.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ->
                    "API Quota limit reached (429 Resource Exhausted). Free tier quotas generally reset at midnight Pacific Time (PT) or within 1 to 24 hours. You can still export using 'Copy Text' or 'Share to Docs'."
                statusCode >= 500 ->
                    "Google Docs server error (HTTP $statusCode). Please try again shortly or use 'Share Text'."
                else ->
                    "Google Docs error ($statusCode): $message"
            }
        } catch (_: Exception) {
            "Google Docs request failed (HTTP $statusCode): ${rawResponse.take(120)}"
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
    }
}
