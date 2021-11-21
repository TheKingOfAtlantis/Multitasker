package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@SmallTest class DateConverterTest : ConverterTest<LocalDate, String>(
    DateConverter, listOf(
        LocalDate.of(2000, 10, 12) to "2000-10-12",
        LocalDate.of(1980, 1, 1)   to "1980-01-01",
        LocalDate.of(2132, 4, 16)  to "2132-04-16"
    )
) {
    @Test override fun withNull() = super.withNull()
    @Test override fun validateConversion() = super.validateConversion()
    @Test override fun inverse() = super.inverse()
}
