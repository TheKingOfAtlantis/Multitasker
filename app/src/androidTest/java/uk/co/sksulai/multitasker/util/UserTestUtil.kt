package uk.co.sksulai.multitasker.util

import java.util.*
import java.time.Instant

import kotlin.random.Random

import uk.co.sksulai.multitasker.db.model.UserModel

object RandomUtil {
    private val DefaultCharSet = (('a'..'z') + ('A'..'Z') + ('0'..'9'))

    fun nextString(min: Int, max: Int, allowed: List<Char> = DefaultCharSet) = nextString(Random.nextInt(min, max), allowed)
    fun nextString(length: Int, allowed: List<Char> = DefaultCharSet): String =
        IntRange(0, length)
            .map { Random.nextInt(until = DefaultCharSet.size) }
            .map(DefaultCharSet::get)
            .joinToString("")
    fun nextEmail(domain: String = "domain.com") = "${nextString(4, 12)}@$domain"
}

object UserTestUtil {
    fun createSingle() = UserModel(
        ID = UUID.randomUUID().toString(),
        Creation = Instant.now(),
        LastModified = Instant.now(),
        null, null, "", null, null, null, null
    )

    fun createList(size: Int) = List(size) { createSingle() }
}
