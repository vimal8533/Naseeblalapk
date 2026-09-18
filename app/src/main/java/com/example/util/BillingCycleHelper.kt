package com.example.util

import java.util.Calendar
import java.util.Date

object BillingCycleHelper {

    val MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun getMonthIndex(monthName: String): Int {
        val idx = MONTHS.indexOfFirst { it.equals(monthName.trim(), ignoreCase = true) }
        return if (idx >= 0) idx else 0
    }

    /**
     * Checks if rent for this tenant should be billed / due in the specified month & year.
     * Monthly: Every month.
     * Quarterly (3M): Every 3rd month from joining date (or Jan/Apr/Jul/Oct).
     * Half-Yearly (6M): Every 6th month from joining date (or Jan/Jul).
     * Yearly (12M): Every 12th month from joining date (or anniversary month).
     */
    fun isRentDueInMonth(
        joiningDateStr: String?,
        cycleMonths: Int,
        targetMonth: String,
        targetYear: Int
    ): Boolean {
        if (cycleMonths <= 1) return true

        val targetMonthIdx = getMonthIndex(targetMonth)
        val joinDate = PmcTaxHelper.parseDate(joiningDateStr)

        if (joinDate != null) {
            val joinCal = Calendar.getInstance().apply { time = joinDate }
            val joinYear = joinCal.get(Calendar.YEAR)
            val joinMonthIdx = joinCal.get(Calendar.MONTH)

            // Total elapsed calendar months between joining date and target month/year
            val elapsedMonths = (targetYear - joinYear) * 12 + (targetMonthIdx - joinMonthIdx)

            if (elapsedMonths < 0) {
                // Tenant joined in a future month
                return false
            }

            return (elapsedMonths % cycleMonths) == 0
        }

        // Fallback if joining date is absent: align to calendar quarters/halves/years
        return when (cycleMonths) {
            3 -> targetMonthIdx % 3 == 0 // Jan (0), Apr (3), Jul (6), Oct (9)
            6 -> targetMonthIdx % 6 == 0 // Jan (0), Jul (6)
            12 -> targetMonthIdx == 0    // Jan (0)
            else -> true
        }
    }

    /**
     * Generates a descriptive Hindi/English cycle string for SMS and displays.
     * e.g. "6 Mahine", "3 Mahine", "Varshik (Yearly)", "October 2026"
     */
    fun getCycleTextForSms(
        cycleMonths: Int,
        month: String,
        year: Int
    ): String {
        return when (cycleMonths) {
            3 -> "3 Mahine"
            6 -> "6 Mahine"
            12 -> "Varshik (Yearly)"
            else -> "$month $year"
        }
    }

    /**
     * Display label for UI tags
     */
    fun getCycleBadge(cycleMonths: Int): String {
        return when (cycleMonths) {
            3 -> "3M Cycle"
            6 -> "6M Cycle"
            12 -> "Yearly"
            else -> "Monthly"
        }
    }
}
