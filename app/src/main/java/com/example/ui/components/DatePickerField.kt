package com.example.ui.components

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.PmcTaxHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Reusable helper to show native Material Calendar DatePickerDialog.
 */
fun showCalendarDatePicker(
    context: Context,
    initialDateStr: String?,
    minDateMillis: Long? = null,
    maxDateMillis: Long? = null,
    dateFormatPattern: String = "dd MMM yyyy",
    onDateSelected: (formattedDate: String, calendar: Calendar) -> Unit
) {
    val cal = Calendar.getInstance()
    val parsed = PmcTaxHelper.parseDate(initialDateStr)
    if (parsed != null) {
        cal.time = parsed
    }

    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            val formatted = SimpleDateFormat(dateFormatPattern, Locale.ENGLISH).format(selectedCal.time)
            onDateSelected(formatted, selectedCal)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    minDateMillis?.let { dialog.datePicker.minDate = it }
    maxDateMillis?.let { dialog.datePicker.maxDate = it }

    dialog.show()
}

/**
 * A beautiful, testable DatePicker field for Jetpack Compose.
 * Clicking anywhere on the field opens the system calendar picker.
 */
@Composable
fun DatePickerField(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    dateFormatPattern: String = "dd MMM yyyy",
    minDateMillis: Long? = null,
    maxDateMillis: Long? = null,
    testTag: String = "date_picker_field"
) {
    val context = LocalContext.current
    val openPicker = {
        if (enabled) {
            showCalendarDatePicker(
                context = context,
                initialDateStr = selectedDate,
                minDateMillis = minDateMillis,
                maxDateMillis = maxDateMillis,
                dateFormatPattern = dateFormatPattern,
                onDateSelected = { formatted, _ ->
                    onDateSelected(formatted)
                }
            )
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            OutlinedTextField(
                value = selectedDate.ifBlank { "Select Date" },
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                isError = isError,
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = openPicker,
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditCalendar,
                            contentDescription = "Pick Date from Calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Transparent overlay to ensure clicking anywhere opens the calendar picker
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = openPicker
                    )
            )
        }

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}
