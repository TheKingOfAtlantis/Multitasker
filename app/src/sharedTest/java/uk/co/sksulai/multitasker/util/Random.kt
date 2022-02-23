package uk.co.sksulai.multitasker.util

import kotlin.random.*

val defaultCharset = (('A'..'Z') + ('a'..'z') + ('0'..'9')).toCharArray()

/**
 * Generates a random string containing values from a specific [charset]
 * with a length in the [range] specified
 */
fun Random.nextString(
    range: IntRange,
    charset: CharArray = defaultCharset
): String = nextString(range.random(), charset)

/**
 * Generates a random string containing values from a specific [charset]
 * with a length in the [range] specified
 */
fun Random.nextString(
    from: Int = 0,
    until: Int,
    charset: CharArray = defaultCharset
): String = nextString(from..until, charset)

/**
 * Generates a random string containing values from a specific [charset]
 * of exactly length [len]
 */
fun Random.nextString(
    len: Int,
    charset: CharArray = defaultCharset
): String = List(len) { charset.random() }.joinToString(separator = "")

/**
 * Generates a random string containing values from a specific [charset].
 * The length of the string will be in the range [0..2^16]
 */
fun Random.nextString(charset: CharArray = defaultCharset) = nextString(until = 65536)

fun Random.nextEmail() = nextString(8..12) + "@domain.com"
