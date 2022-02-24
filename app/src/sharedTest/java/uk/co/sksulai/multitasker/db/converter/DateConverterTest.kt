package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@SmallTest class DateConverterTest : ConverterTest<LocalDate, String>(
    DateConverter,
    testDateList
)
