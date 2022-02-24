package uk.co.sksulai.multitasker.db.converter

import java.time.*
import kotlin.random.Random

const val testListLength = 512

val testDateList get() = List(testListLength) {
    val year = Random.nextInt(4000)
    val month = Random.nextInt(1, 12)
    val day = Random.nextInt(1, YearMonth.of(year, month).lengthOfMonth())

    LocalDate.of(year, month, day) to "%04d-%02d-%02d".format(year, month, day)
}

val testLocalTimeList = List(testListLength) {
    val hour = Random.nextInt(0, 23)
    val min  = Random.nextInt(0, 59)
    val sec  = Random.nextInt(0, 59)

    LocalTime.of(hour, min, sec) to "%02d:%02d:%02d".format(hour, min, sec)
}

val testOffsetTimeList get() = testLocalTimeList.map { (time, str) ->
    val offset = Random.nextInt(-11, 11)
    val offsetStr = if(offset == 0) "Z" else "%+03d:00".format(offset)

    OffsetTime.of(
        time,
        ZoneOffset.ofHours(offset)
    ) to "$str$offsetStr"
}
