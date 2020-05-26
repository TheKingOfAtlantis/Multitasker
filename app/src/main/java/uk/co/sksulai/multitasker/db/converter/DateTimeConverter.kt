package uk.co.sksulai.multitasker.db.converter

import androidx.room.TypeConverter
import java.time.*

class DateTimeConverter : IConverter<LocalDateTime?, String?> {
    @TypeConverter override fun from(value: LocalDateTime?): String? = if(value == null) null else value.toString()
    @TypeConverter override fun to(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
}
class DateConverter : IConverter<LocalDate?, String?> {
    @TypeConverter override fun from(value: LocalDate?): String? = if(value == null) null else value.toString()
    @TypeConverter override fun to(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
class TimeConverter: IConverter<LocalTime?, String?> {
    @TypeConverter override fun from(value: LocalTime?): String? = if(value == null) null else value.toString()
    @TypeConverter override fun to(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }
}
