package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.runner.RunWith

import java.util.*
import java.time.*
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@SmallTest class DurationConverterTest : ConverterTest<Duration, String>(
    DurationConverter,
    List(testListLength) {
        Duration.ofMillis(Random.nextLong()).let { it to it.toString() }
    }
)

@RunWith(AndroidJUnit4::class)
@SmallTest class TimeZoneConverterTest : ConverterTest<TimeZone, String>(
    TimeZoneConverter,
    ZoneId.getAvailableZoneIds().map { TimeZone.getTimeZone(it) to it }
)

@RunWith(AndroidJUnit4::class)
@SmallTest class InstantConverterTest : ConverterTest<Instant, String>(
    InstantConverter,
    List(testListLength) {
        Instant.ofEpochMilli(Random.nextLong()).let { it to it.toString() }
    }
)
