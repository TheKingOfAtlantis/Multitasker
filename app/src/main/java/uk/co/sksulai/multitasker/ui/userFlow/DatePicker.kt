package uk.co.sksulai.multitasker.ui.userFlow

import java.time.*

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker

object DatePicker {
    private fun Long.toLocalDate() =
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    fun single (
        activity: AppCompatActivity,
        title: String,
        initialSelection: LocalDate?,
        constraints: CalendarConstraints? = null,
        onValueChange: (LocalDate) -> Unit
    ) {
        val picker = MaterialDatePicker.Builder.datePicker().apply {
            setTitleText(title)
            constraints?.let { setCalendarConstraints(it) }
            initialSelection?.let {
                val date = it.atTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC))
                    .toInstant()
                    .toEpochMilli()
                setSelection(date)
            }
        }.build()

        picker.show(activity.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { onValueChange(it.toLocalDate()) }
    }
    fun range (
        activity: AppCompatActivity,
        title: String,
        constraints: CalendarConstraints? = null,
        onStartChange: (LocalDate) -> Unit,
        onEndChange: (LocalDate) -> Unit,
    ) {
        val picker = MaterialDatePicker.Builder.dateRangePicker().apply {
            setTitleText(title)
            constraints?.let { setCalendarConstraints(it) }
        }.build()

        picker.show(activity.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            onStartChange(it.first.toLocalDate())
            onEndChange(it.second.toLocalDate())
        }
    }
}
