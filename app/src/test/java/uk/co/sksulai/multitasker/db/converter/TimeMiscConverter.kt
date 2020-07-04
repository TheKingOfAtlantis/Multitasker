package uk.co.sksulai.multitasker.db.converter

import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.hamcrest.Matchers

import java.util.*
import java.time.*

class DurationConverterTest : ConverterTest<Duration, String>(
    DurationConverter(),
    listOf(
        Duration.ofDays(2) to "PT48H",
        Duration.ofHours(6).plusMinutes(30) to "PT6H30M"
    )
) {
    @Test override fun withNull() = super.withNull()
    @Test override fun validateConversion() = super.validateConversion()
    @Test override fun inverse() = super.inverse()
}
