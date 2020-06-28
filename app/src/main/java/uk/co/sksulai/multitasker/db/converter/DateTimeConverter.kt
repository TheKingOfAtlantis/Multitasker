package uk.co.sksulai.multitasker.db.converter

import androidx.room.TypeConverter

import java.time.*
import java.time.format.DateTimeFormatter

class DateTimeConverter : IConverter<LocalDateTime?, String?> {
    @TypeConverter override fun from(value: LocalDateTime?): String? = if(value == null) null else value.toString()
    @TypeConverter override fun to(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
}
// Date converter

class DateConverter : IConverter<LocalDate?, String?> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    @TypeConverter override fun from(value: LocalDate?): String? = value?.let { formatter.format(it) }
    @TypeConverter override fun to(value: String?): LocalDate? = value?.let { formatter.parse(it, LocalDate::from) }
}
}
class LocalTimeConverter: IConverter<LocalTime?, String?> {
    @TypeConverter override fun from(value: LocalTime?): String? = if(value == null) null else value.toString()
    @TypeConverter override fun to(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }
}
