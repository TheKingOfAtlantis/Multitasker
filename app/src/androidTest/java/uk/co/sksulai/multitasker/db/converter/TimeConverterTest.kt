package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

import java.time.*

@RunWith(Suite::class) @Suite.SuiteClasses(
    OffsetTimeConverterTest::class,
    LocalTimeConverterTest::class
) class TimeConverterSuite

@RunWith(AndroidJUnit4::class)
@SmallTest class OffsetTimeConverterTest : ConverterTest<OffsetTime, String>(
    OffsetTimeConverter,
    listOf(
        OffsetTime.of(
            LocalTime.of(10, 10, 10),
            ZoneOffset.ofHours(1)
        ) to "10:10:10+01:00"
    )
)

@RunWith(AndroidJUnit4::class)
@SmallTest class LocalTimeConverterTest : ConverterTest<LocalTime, String>(
    LocalTimeConverter,
    listOf(
        LocalTime.of(16, 43, 12) to "16:43:12",
        LocalTime.of(5,  31, 45) to "05:31:45",
        LocalTime.of(0,   0, 24) to "00:00:24",
        LocalTime.of(23, 10, 58) to "23:10:58"
    )
)
