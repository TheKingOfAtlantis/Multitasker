package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import com.google.common.truth.Truth.assertThat

import java.util.*
import java.time.*

@RunWith(Suite::class) @Suite.SuiteClasses(
    DurationConverterTest::class,
    TimeZoneConverterTest::class
) class TimeMiscConverterSuite

@RunWith(AndroidJUnit4::class)
@SmallTest class DurationConverterTest : ConverterTest<Duration, String>(
    DurationConverter,
    listOf(
        Duration.ofDays(2) to "PT48H",
        Duration.ofHours(6).plusMinutes(30) to "PT6H30M"
    )
) {
    @Test override fun withNull() = super.withNull()
    @Test override fun validateConversion() = super.validateConversion()
    @Test override fun inverse() = super.inverse()
}

@RunWith(AndroidJUnit4::class)
@SmallTest class TimeZoneConverterTest : ConverterTest<TimeZone, String>(
    TimeZoneConverter,
    listOf(
        "GMT",
        "Europe/London"
    ).map { TimeZone.getTimeZone(it) to it }
) {
    @Test override fun withNull() = super.withNull()
    @Test override fun validateConversion() = super.validateConversion()
    @Test override fun inverse() = testData.forEach { (lhs, rhs) ->
        assertThat(converter.from(converter.to(rhs)))
            .isEqualTo(lhs?.id)
        // Fixme: Find work around ?.toString()
        //        Without it values don't match even tho the values are equivalent
        //        but one is surrounded by <...> and the other "..."
        assertThat(converter.to(converter.from(lhs))?.id)
            .isEqualTo(rhs)
    }
}
