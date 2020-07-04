package uk.co.sksulai.multitasker.db.converter

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

import java.time.*

class LocalTimeConverterTest : ConverterTest<LocalTime, String>(
    LocalTimeConverter(),
    listOf(
        LocalTime.of(16, 43, 12) to "16:43:12",
        LocalTime.of(5, 31, 45)  to "05:31:45",
        LocalTime.of(0, 0, 24)   to "00:00:24",
        LocalTime.of(23, 10, 58) to "23:10:58"
    )
) {
    @Test override fun withNull() = super.withNull()
    @Test override fun validateConversion() = super.validateConversion()
    @Test override fun inverse() = super.inverse()
}
