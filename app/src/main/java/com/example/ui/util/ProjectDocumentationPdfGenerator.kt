package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProjectDocumentationPdfGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 height

    /**
     * Generates a comprehensive, executive 3-page Project Documentation PDF
     * detailing system workflow, tech stack, architecture, and interview presentation guide.
     */
    fun generateAndShareDocumentationPdf(context: Context): Uri? {
        try {
            val document = PdfDocument()

            // Page 1: Executive Overview, Problem Statement & Core Workflow
            val page1Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = document.startPage(page1Info)
            drawPage1(page1.canvas)
            document.finishPage(page1)

            // Page 2: Tech Stack, Dual-Channel Automation & Tenant Echo Architecture
            val page2Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val page2 = document.startPage(page2Info)
            drawPage2(page2.canvas)
            document.finishPage(page2)

            // Page 3: Security, RBAC & Interviewer Presentation Playbook
            val page3Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 3).create()
            val page3 = document.startPage(page3Info)
            drawPage3(page3.canvas)
            document.finishPage(page3)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fileName = "NaseebLalMarket_Project_Documentation_$timeStamp.pdf"

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePdfViaMediaStore(context, document, fileName)
            } else {
                savePdfLegacy(context, document, fileName)
            }

            document.close()

            if (uri != null) {
                Toast.makeText(context, "Documentation PDF Downloaded! Opening...", Toast.LENGTH_LONG).show()
                openAndSharePdf(context, uri)
            } else {
                Toast.makeText(context, "PDF save nahi ho saka", Toast.LENGTH_SHORT).show()
            }
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun drawPage1(canvas: Canvas) {
        val margin = 36f
        val contentWidth = PAGE_WIDTH - (margin * 2)

        // Header Background Banner (Navy)
        val headerPaint = Paint().apply {
            color = Color.rgb(10, 25, 47)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 105f, headerPaint)

        // Gold Accent Stripe
        val goldPaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 105f, PAGE_WIDTH.toFloat(), 110f, goldPaint)

        // Header Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("NASEEB LAL MARKET - COMMERCIAL BILLING SUITE", margin, 42f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.rgb(254, 243, 199)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("System Architecture, Operational Workflows & Engineering Specification", margin, 64f, subTitlePaint)

        val metaPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 9.5f
            isAntiAlias = true
        }
        canvas.drawText("Target: Android 14+ | Native Kotlin & Compose | Realtime Cloud Sync | Version 2.4", margin, 86f, metaPaint)

        // Body Content
        var curY = 135f

        curY = drawSectionHeading(canvas, "1. EXECUTIVE OVERVIEW & BUSINESS CONTEXT", margin, curY)
        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            isAntiAlias = true
        }

        val overviewText = "Naseeb Lal Market (NLM) Suite is a mission-critical, enterprise Android mobile management solution built for modern commercial real estate operators. It eliminates manual paper registers, untracked partial dues, delayed sub-meter readings, and disconnected billing disputes. It bridges the operational gap between Market Administrators and tenants through a synchronized, two-way digital communication protocol (Tenant Echo)."
        curY = drawWrappedText(canvas, overviewText, margin, curY, contentWidth, textPaint, 13f) + 8f

        curY = drawSectionHeading(canvas, "2. CORE OPERATIONAL WORKFLOW & APPLICATION LIFECYCLE", margin, curY)

        val workflows = listOf(
            "Step 1: Property & Tenant Onboarding" to "Admin configures commercial shops or residential flats. Defines baseline parameters: monthly rent, advance deposit, contractual annual increment %, billing cycle (Monthly, Quarterly, Yearly), and initial electric sub-meter reading. Supports multi-shop assignments under a single business entity.",
            "Step 2: Dynamic Monthly Cycle Generation" to "Billing cycles dynamically initialize to the active calendar month and year (e.g. October 2026). The system automatically evaluates annual rent increments, aggregates PMC municipal taxes, and prepares ledger line items without manual calculation errors.",
            "Step 3: Sub-Metered Electricity & Utility Calculation" to "Features 1-Click Batch Sub-Meter entry for commercial units and flats. Real-time consumption computation (Units Consumed = Current Reading - Previous Reading). Automatically multiplies tariff rate (₹10/unit) and merges bill directly into tenant monthly ledger.",
            "Step 4: Smart Dues & Partial Payment Reconciliation" to "Distinguishes between Fully Paid, Partially Paid, and Pending dues. Accurately tracks prior uncollected arrears, carried balances, and itemizes breakdown (Rent + Electricity + PMC Tax + Arrears) for 100% financial transparency.",
            "Step 5: Zero-Permission Reminders & Fast2SMS Gateway" to "Dispatches polite, itemized payment reminders via two avenues: (1) Free sequential Android Intent auto-queue (WhatsApp / SMS) requiring zero dangerous permissions, or (2) Fast2SMS Cloud API Gateway for 1-click cloud SMS batch dispatch."
        )

        for ((title, desc) in workflows) {
            val stepTitlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("• $title", margin + 6f, curY, stepTitlePaint)
            curY += 12f
            curY = drawWrappedText(canvas, desc, margin + 18f, curY, contentWidth - 18f, textPaint, 12.5f) + 6f
        }

        drawPageFooter(canvas, 1, 3)
    }

    private fun drawPage2(canvas: Canvas) {
        val margin = 36f
        val contentWidth = PAGE_WIDTH - (margin * 2)

        // Top Mini Banner
        val headerPaint = Paint().apply {
            color = Color.rgb(10, 25, 47)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 45f, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TECHNICAL STACK, ARCHITECTURE & PROTOCOL DESIGN", margin, 28f, titlePaint)

        var curY = 68f

        curY = drawSectionHeading(canvas, "3. PRODUCTION TECHNICAL STACK & LIBRARIES", margin, curY)

        val stackTable = listOf(
            "Layer / Domain" to "Technology / Framework & Engineering Rationale",
            "Programming Language" to "Kotlin 2.0+ (100% Kotlin Coroutines & Flow for asynchronous reactive state)",
            "User Interface Framework" to "Jetpack Compose (Modern declarative UI, Material 3 Design System, Adaptive Insets)",
            "State Management" to "MVVM Architecture with StateFlow, derivedStateOf & lifecycle-aware collection",
            "Cloud Persistence & Sync" to "Google Firebase Firestore Realtime Database with offline caching & optimistic updates",
            "Reporting & Document Engine" to "Native Android Canvas PdfDocument (High-fidelity A4 rendering) + CSV Excel Engine",
            "Telephony & Messaging" to "Android ACTION_SENDTO Intent Queue (Zero-Permission) + Fast2SMS REST API (OkHttp3)",
            "Barcode & Security" to "ZXing Embedded Engine (Cryptographic verification QR Codes on receipts)",
            "Testing & Quality Assurance" to "Robolectric JVM Unit Testing & JUnit4 verifying business logic and state invariants"
        )

        curY = drawTable(canvas, stackTable, margin, curY, contentWidth) + 14f

        curY = drawSectionHeading(canvas, "4. TWO-WAY 'TENANT ECHO' SELF-SERVICE PROTOCOL", margin, curY)
        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            isAntiAlias = true
        }

        val echoExplanation = "Tenant Echo is a proprietary two-way interaction layer that connects tenants with their account without requiring them to install an app or remember passwords. Each tenant is provided a dedicated, tamper-proof portal link (e.g. naseeblalmarket.web.app/portal?echo={tenantId}_{month}_{year})."
        curY = drawWrappedText(canvas, echoExplanation, margin, curY, contentWidth, textPaint, 13f) + 8f

        val echoFeatures = listOf(
            "Public Transparency" to "Tenant views full monthly ledger: Base Rent, Electricity Meter Units & Consumption, PMC Property Tax, and past payment history.",
            "Digital Promise-to-Pay" to "Tenant can select an expected payment commitment date. When submitted, the Admin dashboard automatically pauses batch reminder notifications until the promised date has elapsed.",
            "Anti-Fraud Verification Queue" to "Tenant can submit a 'Claimed Paid' notification with UTR/receipt reference. The bill is flagged in an Amber verification queue on the Admin dashboard, preventing accidental double-billing or awkward reminder calls.",
            "Instant PDF Rent Receipts" to "Upon verification, tenants can generate and download authentic QR-verified PDF receipts signed by Naseeb Lal Market administration directly from their web portal."
        )

        for ((feature, desc) in echoFeatures) {
            val fTitlePaint = Paint().apply {
                color = Color.rgb(30, 58, 138)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("✓ $feature:", margin + 6f, curY, fTitlePaint)
            curY += 12f
            curY = drawWrappedText(canvas, desc, margin + 18f, curY, contentWidth - 18f, textPaint, 12.5f) + 6f
        }

        drawPageFooter(canvas, 2, 3)
    }

    private fun drawPage3(canvas: Canvas) {
        val margin = 36f
        val contentWidth = PAGE_WIDTH - (margin * 2)

        // Top Mini Banner
        val headerPaint = Paint().apply {
            color = Color.rgb(10, 25, 47)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 45f, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("SECURITY, RBAC & INTERVIEW PRESENTATION PLAYBOOK", margin, 28f, titlePaint)

        var curY = 68f

        curY = drawSectionHeading(canvas, "5. ENTERPRISE RBAC & TAMPER-PROOF AUDIT TRAILS", margin, curY)
        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            isAntiAlias = true
        }

        val rbacDesc = "• Dual Role Architecture: Master Administrator retains full authority (tenant archiving, permanent deletion, contract edits, sub-admin delegation). Delegated Sub-Admins operate in restricted collection & metering modes.\n• Immutable Audit Trail: Every collection, meter reading update, payment correction, and notice dispatch creates an un-editable ActivityLog with timestamp, user identity, action type, and before/after delta."
        curY = drawWrappedText(canvas, rbacDesc, margin, curY, contentWidth, textPaint, 13f) + 12f

        curY = drawSectionHeading(canvas, "6. INTERVIEWER PRESENTATION GUIDE (HOW TO PRESENT THIS IN INTERVIEWS)", margin, curY)

        val pitchBoxPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        val pitchBoxBorder = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val pitchRect = RectF(margin, curY, margin + contentWidth, curY + 115f)
        canvas.drawRoundRect(pitchRect, 8f, 8f, pitchBoxPaint)
        canvas.drawRoundRect(pitchRect, 8f, 8f, pitchBoxBorder)

        val pitchHeaderPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("🎯 30-Second Elevator Pitch for Hiring Managers & Interviewers:", margin + 10f, curY + 18f, pitchHeaderPaint)

        val pitchBody = "\"I engineered Naseeb Lal Market Suite, a production-grade commercial property ERP on modern Android (Kotlin & Jetpack Compose). I designed it to solve real-world billing bottlenecks: handling dynamic annual rent increments, multi-tenant sub-metering, partial dues tracking, and a two-way digital communication protocol called Tenant Echo that eliminates tenant disputes with QR-verified digital receipts. The app features cloud synchronization with Firebase Firestore, zero-permission sequential automation, and full offline-first resilience.\""
        drawWrappedText(canvas, pitchBody, margin + 10f, curY + 34f, contentWidth - 20f, textPaint, 12f)
        curY += 128f

        val technicalHighlights = listOf(
            "State & Performance" to "Employed unidirectional data flow with StateFlow and immutable UI states. Used associateBy lookup maps and derivedStateOf to ensure smooth 60fps scrolling across 100+ units.",
            "Zero-Permission Strategy" to "Engineered an intelligent sequential auto-queue using Android ACTION_SENDTO and URL-encoded WhatsApp intents, bypassing dangerous runtime SMS permissions and adhering to strict Google Play policies.",
            "Reliability & Resilience" to "Implemented soft-delete archive mechanisms with 30-day recovery windows, audit trails for financial accountability, and robust automated JVM test coverage via Robolectric."
        )

        for ((hTitle, hDesc) in technicalHighlights) {
            val qTitlePaint = Paint().apply {
                color = Color.rgb(180, 83, 9)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("★ $hTitle:", margin + 6f, curY, qTitlePaint)
            curY += 12f
            curY = drawWrappedText(canvas, hDesc, margin + 18f, curY, contentWidth - 18f, textPaint, 12.5f) + 6f
        }

        // Contact / Admin Sign-off strip
        curY += 8f
        val signOffPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Engineered for: Naseeb Lal Market • Master Administrator: Vimal Kumar (Mob: 7654138539)", margin, curY, signOffPaint)

        drawPageFooter(canvas, 3, 3)
    }

    private fun drawSectionHeading(canvas: Canvas, heading: String, x: Float, y: Float): Float {
        val paint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(heading, x, y, paint)

        val underlinePaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            strokeWidth = 1.2f
        }
        canvas.drawLine(x, y + 3f, x + paint.measureText(heading), y + 3f, underlinePaint)
        return y + 15f
    }

    private fun drawTable(canvas: Canvas, rows: List<Pair<String, String>>, startX: Float, startY: Float, width: Float): Float {
        var currentY = startY
        val col1Width = 140f
        val col2Width = width - col1Width

        val headerBg = Paint().apply { color = Color.rgb(241, 245, 249) }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        for ((index, row) in rows.withIndex()) {
            val isHeader = index == 0
            val rowHeight = if (isHeader) 20f else 18f

            if (isHeader) {
                canvas.drawRect(startX, currentY, startX + width, currentY + rowHeight, headerBg)
            }
            canvas.drawRect(startX, currentY, startX + width, currentY + rowHeight, borderPaint)
            canvas.drawLine(startX + col1Width, currentY, startX + col1Width, currentY + rowHeight, borderPaint)

            val textPaint = Paint().apply {
                color = if (isHeader) Color.rgb(15, 23, 42) else Color.rgb(51, 65, 85)
                textSize = if (isHeader) 9f else 8.5f
                typeface = if (isHeader) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
                isAntiAlias = true
            }

            canvas.drawText(row.first, startX + 6f, currentY + rowHeight - 6f, textPaint)
            canvas.drawText(row.second, startX + col1Width + 6f, currentY + rowHeight - 6f, textPaint)

            currentY += rowHeight
        }
        return currentY
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint, lineHeight: Float): Float {
        val words = text.split(" ")
        var currentY = startY
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)

            if (width < maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                canvas.drawText(currentLine.toString(), x, currentY, paint)
                currentY += lineHeight
                currentLine = StringBuilder(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, currentY, paint)
            currentY += lineHeight
        }

        return currentY
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }
        canvas.drawLine(36f, PAGE_HEIGHT - 35f, PAGE_WIDTH - 36f, PAGE_HEIGHT - 35f, linePaint)

        val footerPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("Naseeb Lal Market Billing Suite • Confidential & Proprietary", 36f, PAGE_HEIGHT - 20f, footerPaint)

        val pageStr = "Page $pageNumber of $totalPages"
        val pageStrWidth = footerPaint.measureText(pageStr)
        canvas.drawText(pageStr, PAGE_WIDTH - 36f - pageStrWidth, PAGE_HEIGHT - 20f, footerPaint)
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

    private fun openAndSharePdf(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Naseeb Lal Market - Project Architecture & Workflow Documentation")
                putExtra(Intent.EXTRA_TEXT, "Attached is the complete technical documentation and architecture report for Naseeb Lal Market Billing Suite.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open or Share Project Documentation PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
