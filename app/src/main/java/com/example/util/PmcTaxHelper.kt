package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PmcTaxHelper {

    const val PMC_TAX_AMOUNT = 1000.0

    private val supportedFormats = listOf(
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("MMMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()),
        SimpleDateFormat("MMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("MMM yyyy", Locale.getDefault())
    )

    /**
     * Parses a date string using multiple fallback formats.
     */
    fun parseDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        val cleanStr = dateStr.trim()
        for (format in supportedFormats) {
            try {
                format.isLenient = true
                val parsed = format.parse(cleanStr)
                if (parsed != null) return parsed
            } catch (_: Exception) {
                // Try next format
            }
        }
        return null
    }

    /**
     * Checks whether 1 full year (365 days or 12 calendar months) has elapsed since joining date.
     */
    fun hasCompletedOneYear(joiningDateStr: String?, referenceDate: Date = Date()): Boolean {
        val joinDate = parseDate(joiningDateStr) ?: return false
        val joinCal = Calendar.getInstance().apply { time = joinDate }
        val refCal = Calendar.getInstance().apply { time = referenceDate }

        // Add 1 full year to joining date
        joinCal.add(Calendar.YEAR, 1)

        // If reference date is on or after (joiningDate + 1 year), 1 year is completed
        return !refCal.before(joinCal)
    }

    /**
     * Resolves a month string (e.g. "September", "Sep", "09") to a 0-indexed calendar month (0..11).
     */
    fun getMonthIndex(monthName: String): Int? {
        val clean = monthName.trim().lowercase(Locale.ENGLISH)
        val months = listOf(
            "jan", "feb", "mar", "apr", "may", "jun",
            "jul", "aug", "sep", "oct", "nov", "dec"
        )
        val index = months.indexOfFirst { clean.startsWith(it) }
        return if (index >= 0) index else null
    }

    /**
     * PMC Tax is an ANNUAL tax (applied ONLY ONCE A YEAR on the anniversary month, not every month).
     * Returns true ONLY if at least 1 full year has elapsed AND the target month is the anniversary month.
     */
    fun isPmcTaxDueInMonth(joiningDateStr: String?, targetMonthName: String, targetYear: Int): Boolean {
        val joinDate = parseDate(joiningDateStr) ?: return false
        val joinCal = Calendar.getInstance().apply { time = joinDate }
        val joinYear = joinCal.get(Calendar.YEAR)
        val joinMonth = joinCal.get(Calendar.MONTH) // 0-indexed

        // If target year is less than joinYear + 1, 1 full year hasn't passed
        if (targetYear < joinYear + 1) return false

        // Match targetMonthName to calendar month (0..11)
        val targetMonthIndex = getMonthIndex(targetMonthName) ?: return false
        return targetMonthIndex == joinMonth
    }

    /**
     * Returns total annual PMC tax based on number of shops held by the tenant.
     * Rate: ₹1,000 per shop (billed ONCE A YEAR).
     */
    fun getPmcTaxForShops(numberOfShops: Int): Double {
        return PMC_TAX_AMOUNT * numberOfShops.coerceAtLeast(1)
    }

    /**
     * Calculates effective monthly rent including ₹1,000 per shop PMC tax if 1 year is completed.
     * Returns: (Total Amount, hasPmcTaxApplied)
     */
    fun calculateEffectiveRent(
        baseRent: Double,
        joiningDateStr: String?,
        numberOfShops: Int = 1,
        referenceDate: Date = Date()
    ): Pair<Double, Boolean> {
        val completed = hasCompletedOneYear(joiningDateStr, referenceDate)
        val tax = if (completed) getPmcTaxForShops(numberOfShops) else 0.0
        val total = baseRent + tax
        return Pair(total, completed)
    }

    /**
     * Formats tenure string for display, e.g. "1 year 2 months" or "8 months"
     */
    fun getTenureText(joiningDateStr: String?, referenceDate: Date = Date()): String {
        val joinDate = parseDate(joiningDateStr) ?: return "New Tenant"
        val startCal = Calendar.getInstance().apply { time = joinDate }
        val endCal = Calendar.getInstance().apply { time = referenceDate }

        var years = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        var months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)

        if (months < 0) {
            years -= 1
            months += 12
        }

        return when {
            years >= 1 && months > 0 -> "$years yr $months mo"
            years >= 1 -> "$years yr"
            months > 0 -> "$months mo"
            else -> "< 1 mo"
        }
    }

    /**
     * Returns current Indian Financial Year string, e.g. "2026-2027".
     * FY starts on April 1 and ends on March 31.
     */
    fun getCurrentFinancialYear(referenceDate: Date = Date()): String {
        val cal = Calendar.getInstance().apply { time = referenceDate }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // Jan=0, Apr=3
        return if (month >= Calendar.APRIL) {
            "$year-${year + 1}"
        } else {
            "${year - 1}-$year"
        }
    }

    /**
     * Checks if current date falls within the PMC Tax Payment Window:
     * 1st April to 30th September (inclusive).
     */
    fun isPmcTaxNotificationPeriod(referenceDate: Date = Date()): Boolean {
        val cal = Calendar.getInstance().apply { time = referenceDate }
        val month = cal.get(Calendar.MONTH) // Calendar.APRIL (3) to Calendar.SEPTEMBER (8)
        return month in Calendar.APRIL..Calendar.SEPTEMBER
    }
}
