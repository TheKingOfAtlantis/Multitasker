package uk.co.sksulai.multitasker.db.converter

import androidx.room.TypeConverter

import java.util.*
import java.time.*
import java.time.format.DateTimeFormatter

// Date-Time converters

object ZonedDateTimeConverter : IConverter<ZonedDateTime?, String?> {
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    @TypeConverter override fun from(value: ZonedDateTime?): String? = value?.let(formatter::format)
    @TypeConverter override fun to(value: String?): ZonedDateTime?   = value?.let { formatter.parse(it, ZonedDateTime::from) }
}
object OffsetDateTimeConverter : IConverter<OffsetDateTime?, String?> {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    @TypeConverter override fun from(value: OffsetDateTime?): String? = value?.let(formatter::format)
    @TypeConverter override fun to(value: String?): OffsetDateTime?   = value?.let { formatter.parse(it, OffsetDateTime::from) }
}
object LocalDateTimeConverter : IConverter<LocalDateTime?, String?> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    @TypeConverter override fun from(value: LocalDateTime?): String? = value?.let(formatter::format)
    @TypeConverter override fun to(value: String?): LocalDateTime?   = value?.let { formatter.parse(it, LocalDateTime::from) }
}

// Time converters

object OffsetTimeConverter : IConverter<OffsetTime?, String?> {
    private val formatter = DateTimeFormatter.ISO_OFFSET_TIME
    @TypeConverter override fun from(value: OffsetTime?): String? = value?.let(formatter::format)
    @TypeConverter override fun to(value: String?): OffsetTime?   = value?.let { formatter.parse(it, OffsetTime::from) }
}
object LocalTimeConverter : IConverter<LocalTime?, String?> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
    @TypeConverter override fun from(value: LocalTime?): String? = value?.let(formatter::format)
    @TypeConverter override fun to(value: String?): LocalTime?   = value?.let { formatter.parse(it, LocalTime::from) }
}

// Date converter

object DateConverter : IConverter<LocalDate?, String?> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    @TypeConverter override fun from(value: LocalDate?): String? = value?.let(formatter::format)
    @TypeConverter override fun to(value: String?): LocalDate?   = value?.let { formatter.parse(it, LocalDate::from) }
}

// Misc converter

object TimeZoneConverter : IConverter<TimeZone?, String?> {
    @TypeConverter override fun from(value: TimeZone?): String? = value?.let(TimeZone::toZoneId)?.let(ZoneId::toString)
    @TypeConverter override fun to(value: String?): TimeZone?   = value?.let(TimeZone::getTimeZone)
}
object DurationConverter : IConverter<Duration?, String?> {
    @TypeConverter override fun from(value: Duration?): String? = value?.toString()
    @TypeConverter override fun to(value: String?): Duration?   = value?.let(Duration::parse)
}
object InstantConverter : IConverter<Instant?, String?> {
    @TypeConverter override fun from(value: Instant?): String? = value?.toString()
    @TypeConverter override fun to(value: String?): Instant?   = value?.let(Instant::parse)
}
