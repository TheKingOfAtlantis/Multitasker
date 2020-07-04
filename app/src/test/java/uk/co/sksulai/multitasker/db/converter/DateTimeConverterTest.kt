package uk.co.sksulai.multitasker.db.converter

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

import java.time.*
class LocalDateTimeConverterTest : ConverterTest<LocalDateTime, String>(
    LocalDateTimeConverter(),
    listOf(
        LocalDateTime.of(
            LocalDate.of(2020, 6, 29),
            LocalTime.of(10, 10, 10)
        ) to "2020-06-29T10:10:10"
    )
){
    @Test override fun withNull() = super.withNull()
    @Test override fun validateConversion() = super.validateConversion()
    @Test override fun inverse() = super.inverse()
}
