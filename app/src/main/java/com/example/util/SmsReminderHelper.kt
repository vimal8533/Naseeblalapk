package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SimInfo(
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val slotIndex: Int
)

object SmsReminderHelper {

    private const val TAG = "SmsReminderHelper"
    private const val PREFS_NAME = "fast2sms_prefs"
    private const val KEY_FAST2SMS_API_KEY = "fast2sms_api_key"
    const val DEFAULT_FAST2SMS_KEY = "SyO4Vabel9zv65BoKjJXtrC7uMpW8RDEnGimgY3NhxTHqAUkPcXPLrcwnYV46uvWiDphxZCyMjQNzKta"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun getFast2SmsApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_FAST2SMS_API_KEY, "")?.trim() ?: ""
        return if (saved.isNotBlank()) saved else DEFAULT_FAST2SMS_KEY
    }

    fun saveFast2SmsApiKey(context: Context, apiKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FAST2SMS_API_KEY, apiKey.trim()).apply()
    }

    /**
     * Builds a personalized, polite, and clear Hindi/Hinglish SMS for the tenant.
     * CRITICAL: Strictly NO UPI ID or payment links are included.
     */
    fun formatReminderMessage(
        tenantName: String,
        shopNumber: String,
        month: String,
        year: Int,
        pendingAmount: Double,
        senderName: String = "",
        senderContactPhone: String = "",
        cycleMonths: Int = 1,
        isPersonal: Boolean = false,
        electricityBill: Double = 0.0,
        flatBaseRent: Double = 0.0
    ): String {
        val cleanName = tenantName.trim().ifBlank { "Kirayedaar" }
        val cleanShop = shopNumber.trim().ifBlank { if (isPersonal) "Flat" else "Dukaan" }
        val amountStr = if (pendingAmount % 1.0 == 0.0) {
            String.format("%,d", pendingAmount.toLong())
        } else {
            String.format("%,.2f", pendingAmount)
        }

        val trimmedName = senderName.trim()
        val trimmedPhone = senderContactPhone.trim()
        val contactDetails = when {
            trimmedName.isNotBlank() && trimmedPhone.isNotBlank() -> "$trimmedName, Mob: $trimmedPhone"
            trimmedPhone.isNotBlank() -> "Mob: $trimmedPhone"
            trimmedName.isNotBlank() -> trimmedName
            else -> ""
        }
        val contactSuffix = if (contactDetails.isNotBlank()) {
            " (Sampark: $contactDetails)"
        } else {
            ""
        }

        val cyclePhrase = when (cycleMonths) {
            3 -> "3 Mahine ka"
            6 -> "6 Mahine ka"
            12 -> "Varshik (Yearly)"
            else -> "$month $year ka"
        }

        return if (isPersonal) {
            val flatLabel = if (cleanShop.startsWith("Flat", ignoreCase = true)) cleanShop else "Flat $cleanShop"
            val breakdownStr = if (electricityBill > 0.0 && flatBaseRent > 0.0) {
                val rentStr = if (flatBaseRent % 1.0 == 0.0) String.format("%,d", flatBaseRent.toLong()) else String.format("%,.2f", flatBaseRent)
                val elecStr = if (electricityBill % 1.0 == 0.0) String.format("%,d", electricityBill.toLong()) else String.format("%,.2f", electricityBill)
                "kiraya Rs $rentStr + Bijli Bill Rs $elecStr (Kul Rs $amountStr)"
            } else {
                "kiraya Rs $amountStr"
            }
            val signOff = if (trimmedName.isNotBlank()) "Shukriya - $trimmedName" else "Shukriya"
            "Namaste $cleanName ji, aapke $flatLabel ka $cyclePhrase $breakdownStr due hai. Kripya samay par jama karein. $signOff$contactSuffix"
        } else {
            "Namaste $cleanName ji, aapki Shop No. $cleanShop ka $cyclePhrase kiraya Rs $amountStr due hai. Kripya samay par jama karein. Shukriya - Naseeb Lal Market$contactSuffix"
        }
    }

    /**
     * Inspects device for active SIM cards (e.g. SIM 1 / SIM 2)
     */
    fun getAvailableSims(context: Context): List<SimInfo> {
        val simList = mutableListOf<SimInfo>()
        try {
            if (context.checkCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val subs = sm?.activeSubscriptionInfoList
                subs?.forEachIndexed { index, sub ->
                    val name = sub.displayName?.toString()?.ifBlank { "SIM ${index + 1}" } ?: "SIM ${index + 1}"
                    val carrier = sub.carrierName?.toString()?.ifBlank { "Cellular" } ?: "Cellular"
                    simList.add(
                        SimInfo(
                            subscriptionId = sub.subscriptionId,
                            displayName = "$name ($carrier)",
                            carrierName = carrier,
                            slotIndex = sub.simSlotIndex
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching SIM subscriptions: ${e.message}")
        }

        if (simList.isEmpty()) {
            simList.add(
                SimInfo(
                    subscriptionId = -1,
                    displayName = "Default Phone SIM",
                    carrierName = "Active SIM",
                    slotIndex = 0
                )
            )
        }
        return simList
    }

    /**
     * Sends SMS via Fast2SmsApi Retrofit interface.
     * Uses Moshi and OkHttp under the hood for clean type-safe networking.
     */
    suspend fun sendViaFast2SmsRetrofit(
        apiKey: String,
        rawPhoneNumber: String,
        message: String
    ): Pair<Boolean, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        sendViaFast2Sms(apiKey, rawPhoneNumber, message)
    }

    /**
     * Sends SMS via Fast2SMS Cloud API (Quick SMS without DLT).
     * Requires ZERO device permissions (uses standard INTERNET).
     * Play Protect will NEVER flag this!
     */
    fun sendViaFast2Sms(
        apiKey: String,
        rawPhoneNumber: String,
        message: String
    ): Pair<Boolean, String> {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return Pair(false, "Fast2SMS API Key missing")
        }

        val cleanNumber = sanitizePhoneNumber(rawPhoneNumber)
        if (cleanNumber.length < 10) {
            return Pair(false, "Invalid phone number: $rawPhoneNumber")
        }

        return try {
            val formBody = FormBody.Builder()
                .add("route", "q")
                .add("message", message)
                .add("language", "english")
                .add("flash", "0")
                .add("numbers", cleanNumber)
                .build()

            val request = Request.Builder()
                .url("https://www.fast2sms.com/dev/bulkV2")
                .addHeader("authorization", cleanKey)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cache-Control", "no-cache")
                .post(formBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            Log.d(TAG, "Fast2SMS response: code=${response.code}, body=$responseBody")

            if (response.isSuccessful && responseBody.contains("\"return\":true", ignoreCase = true)) {
                Pair(true, "SMS sent successfully")
            } else {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    val msgArray = json.optJSONArray("message")
                    if (msgArray != null && msgArray.length() > 0) {
                        msgArray.optString(0)
                    } else {
                        val strMsg = json.optString("message", "")
                        if (strMsg.isNotBlank()) strMsg else "Fast2SMS Error (HTTP ${response.code})"
                    }
                } catch (_: Exception) {
                    "Fast2SMS Error: HTTP ${response.code}"
                }
                Pair(false, errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed calling Fast2SMS: ${e.message}", e)
            Pair(false, e.message ?: "Network error")
        }
    }

    /**
     * Opens native Messages app with number and text prefilled.
     * Zero permissions required!
     */
    fun openNativeSms(context: Context, rawPhoneNumber: String, message: String) {
        try {
            val cleanNumber = sanitizePhoneNumber(rawPhoneNumber)
            val uri = Uri.parse("smsto:$cleanNumber")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening SMS intent: ${e.message}")
            Toast.makeText(context, "SMS app open nahi ho paya", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens WhatsApp with number and text prefilled.
     * Zero permissions required!
     */
    fun openWhatsApp(context: Context, rawPhoneNumber: String, message: String) {
        try {
            val cleanNumber = sanitizePhoneNumber(rawPhoneNumber)
            val fullNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
            val encodedText = URLEncoder.encode(message, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$fullNumber&text=$encodedText"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening WhatsApp: ${e.message}")
            Toast.makeText(context, "WhatsApp open nahi ho paya", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Dispatches an SMS using standard Android SmsManager
     */
    fun sendSms(
        context: Context,
        rawPhoneNumber: String,
        message: String,
        subscriptionId: Int? = null
    ): Boolean {
        val cleanNumber = sanitizePhoneNumber(rawPhoneNumber)
        if (cleanNumber.isBlank() || cleanNumber.length < 10) {
            Log.w(TAG, "Invalid phone number: $rawPhoneNumber")
            return false
        }

        return try {
            val smsManager: SmsManager = when {
                subscriptionId != null && subscriptionId != -1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    context.getSystemService(SmsManager::class.java)
                }
                else -> {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanNumber, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $cleanNumber: ${e.message}")
            false
        }
    }

    private fun sanitizePhoneNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length == 10 -> digits
            digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
            digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
            else -> digits
        }
    }
}
