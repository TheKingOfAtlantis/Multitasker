package uk.co.sksulai.multitasker.db.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

import java.time.*
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@SmallTest class OffsetTimeConverterTest : ConverterTest<OffsetTime, String>(
    OffsetTimeConverter,
    testOffsetTimeList
)

@RunWith(AndroidJUnit4::class)
@SmallTest class LocalTimeConverterTest : ConverterTest<LocalTime, String>(
    LocalTimeConverter,
    testLocalTimeList
)
