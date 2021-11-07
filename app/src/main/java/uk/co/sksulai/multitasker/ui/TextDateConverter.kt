package uk.co.sksulai.multitasker.ui

import uk.co.sksulai.multitasker.db.converter.IConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object TextDateConverter : IConverter<LocalDate?, String> {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    override fun from(value: LocalDate?): String = value?.let { formatter.format(it) } ?: ""
    override fun to(value: String): LocalDate?   = value.takeIf { it.isNotEmpty() }?.let { formatter.parse(it, LocalDate::from) }
}
