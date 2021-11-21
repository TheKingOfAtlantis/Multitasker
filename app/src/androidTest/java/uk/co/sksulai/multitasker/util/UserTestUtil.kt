package uk.co.sksulai.multitasker.util

import uk.co.sksulai.multitasker.db.model.UserModel
import java.time.Instant
import java.util.*

object UserTestUtil {
    fun createSingle() = UserModel(
        ID = UUID.randomUUID().toString(),
        Creation = Instant.now(),
        LastModified = Instant.now(),
        null, null, "", null, null, null, null
    )

    fun createList(size: Int) = List(size) { createSingle() }
}
