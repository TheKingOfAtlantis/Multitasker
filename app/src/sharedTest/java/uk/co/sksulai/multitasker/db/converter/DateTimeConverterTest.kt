package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

import java.time.*

@RunWith(AndroidJUnit4::class)
@SmallTest class ZonedDateTimeConverterTest : ConverterTest<ZonedDateTime, String>(
    ZonedDateTimeConverter,
    listOf(
        ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.of(2020, 6, 29),
                LocalTime.of(10, 10, 10)
            ),
            ZoneId.of("UTC+1")
        ) to "2020-06-29T10:10:10+01:00[UTC+01:00]"
    )
)

@RunWith(AndroidJUnit4::class)
@SmallTest class OffsetDateTimeConverterTest : ConverterTest<OffsetDateTime, String>(
    OffsetDateTimeConverter,
    listOf(
        OffsetDateTime.of(
            LocalDateTime.of(
                LocalDate.of(2020, 6, 29),
                LocalTime.of(10, 10, 10)
            ),
            ZoneOffset.ofHours(1)
        ) to "2020-06-29T10:10:10+01:00"
    )
)

@RunWith(AndroidJUnit4::class)
@SmallTest class LocalDateTimeConverterTest : ConverterTest<LocalDateTime, String>(
    LocalDateTimeConverter,
    listOf(
        LocalDateTime.of(
            LocalDate.of(2020, 6, 29),
            LocalTime.of(10, 10, 10)
        ) to "2020-06-29T10:10:10"
    )
)
