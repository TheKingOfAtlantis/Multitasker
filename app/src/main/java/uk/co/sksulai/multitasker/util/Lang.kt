package uk.co.sksulai.multitasker.util

import java.util.*
import java.time.*

fun String.ifNotEmpty(operation: (String) -> String) : String =
    if(isNotEmpty()) operation(this) else ""

val ZonedDateTime.timezone: TimeZone get() = TimeZone.getTimeZone(zone)
val OffsetDateTime.timezone: TimeZone get() = TimeZone.getTimeZone(offset)
