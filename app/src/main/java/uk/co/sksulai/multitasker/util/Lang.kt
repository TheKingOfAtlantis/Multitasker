package uk.co.sksulai.multitasker.util

import java.util.*
import java.time.*

fun String.ifNotEmpty(operation: (String) -> String) : String =
    if(isNotEmpty()) operation(this) else ""

/**
 * Rotates the values of an array by a given amount.
 *
 * This method takes the values of an array and offsets their index values by
 * a given amount. Any which are offset beyond the size of the array are wrapped
 * back to the start.
 *
 * Example: {1, 2, 3, 4, 5, 6} rotated by 2 -> {5, 6, 1, 2, 3}
 *
 * @param value The amount to rotate they array by
 */
inline fun <reified T> Array<T>.rotate(value: Int) = Array(size) { this[(it + value) % size] }
/**
 * Rotates the values of an list by a given amount.
 *
 * This method takes the values of a list and offsets their index values by
 * a given amount. Any which are offset beyond the size of the array are wrapped
 * back to the start.
 *
 * Example: {1, 2, 3, 4, 5, 6} rotated by 2 -> {5, 6, 1, 2, 3}
 *
 * @param value The amount to rotate they list by
 */
inline fun <reified T> List<T>.rotate(value: Int)  = List(size) { this[(it + value) % size] }

val ZonedDateTime.timezone: TimeZone get() = TimeZone.getTimeZone(zone)
val OffsetDateTime.timezone: TimeZone get() = TimeZone.getTimeZone(offset)
