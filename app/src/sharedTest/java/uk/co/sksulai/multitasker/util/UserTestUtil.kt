package uk.co.sksulai.multitasker.util

import java.util.*
import java.time.Instant

import kotlin.random.Random
import kotlinx.coroutines.tasks.await

import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

import uk.co.sksulai.multitasker.db.model.UserModel

data class AuthParam(
    val email: String,
    val password: String
) {
    companion object {
        val random get() = AuthParam(
            email = Random.nextEmail(),
            password = Random.nextString(8..12)
        )
    }
}

object UserTestUtil {
    suspend fun createSingle(useAuth: Boolean = false): UserModel = if(!useAuth) UserModel(
        ID = UUID.randomUUID().toString(),
        Creation = Instant.now(),
        LastModified = Instant.now(),
        null, null, "", null, null, null, null
    ) else AuthParam.random.let { auth -> Firebase.auth.createUserWithEmailAndPassword(auth.email, auth.password).await().let {
        createSingle().copy(
            ID     = it.user!!.uid,
            Email  = it.user?.email,
            Avatar = it.user?.photoUrl,
            DisplayName = it.user?.displayName,
        )
    } }

    suspend fun createList(size: Int, useAuth: Boolean = false) = List(size) { createSingle(useAuth) }
}
