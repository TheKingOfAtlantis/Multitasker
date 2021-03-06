package uk.co.sksulai.multitasker.db.converter

import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.time.LocalDateTime

@RunWith(Suite::class) @Suite.SuiteClasses(
    DateConverterTest::class,
    TimeConverterSuite::class,
    DateTimeConverterSuite::class,
    TimeMiscConverterSuite::class,

    UriConverterTest::class
) class ConverterTestSuite
