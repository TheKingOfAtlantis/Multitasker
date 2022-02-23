package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

import java.util.*
import java.time.*

@RunWith(AndroidJUnit4::class)
@SmallTest class DurationConverterTest : ConverterTest<Duration, String>(
    DurationConverter,
    listOf(
        Duration.ofDays(2) to "PT48H",
        Duration.ofHours(6).plusMinutes(30) to "PT6H30M"
    )
)

@RunWith(AndroidJUnit4::class)
@SmallTest class TimeZoneConverterTest : ConverterTest<TimeZone, String>(
    TimeZoneConverter,
    listOf(
        "GMT",
        "Europe/London",
    ).map { TimeZone.getTimeZone(it) to it }
)

@RunWith(AndroidJUnit4::class)
@SmallTest class InstantConverterTest : ConverterTest<Instant, String>(
    InstantConverter,
    listOf(
        Instant.ofEpochMilli(10000L) to "1970-01-01T00:00:10Z",
        Instant.ofEpochMilli(20000L) to "1970-01-01T00:00:20Z",
        Instant.ofEpochMilli(30000L) to "1970-01-01T00:00:30Z",
        Instant.ofEpochMilli(40000L) to "1970-01-01T00:00:40Z",
        Instant.ofEpochMilli(50000L) to "1970-01-01T00:00:50Z",
    )
)
