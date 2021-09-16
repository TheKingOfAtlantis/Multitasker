package uk.co.sksulai.multitasker.db.web

import java.time.Instant

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import uk.co.sksulai.multitasker.db.converter.DateConverter
import uk.co.sksulai.multitasker.db.converter.UriConverter
import uk.co.sksulai.multitasker.db.model.UserModel

fun Timestamp.toInstance() = Instant.ofEpochSecond(seconds, nanoseconds.toLong())

@OptIn(ExperimentalCoroutinesApi::class)
class UserWebService {
    private val db = Firebase.firestore
    private val collection = db.collection("users")

    private fun UserModel.toDocument() = hashMapOf(
        "Email"         to Email,
        "Creation"      to Timestamp(Creation.epochSecond, Creation.nano),
        "LastModified"  to Timestamp(LastModified.epochSecond, LastModified.nano),
        "DisplayName"   to DisplayName,
        "PreferredHome" to PreferredHome,
        "ActualName"    to ActualName,
        "Avatar"        to UriConverter.from(Avatar),
        "DOB"           to DateConverter.from(DOB),
        "Home"          to Home
    )
    private fun MutableMap<String, Any?>.fromDocument(id: String) = UserModel(
        ID            = id,
        Creation      = (get("Creation") as Timestamp).toInstance(),
        LastModified  = (get("LastModified") as Timestamp).toInstance(),
        Email         = get("Email") as String?,
        DisplayName   = get("DisplayName") as String?,
        PreferredHome = get("PreferredHome") as String,
        ActualName    = get("ActualName") as String?,
        Avatar        = UriConverter.to(get("Avatar") as String?),
        DOB           = DateConverter.to(get("DOB") as String?),
        Home          = get("Home") as String?
    )


    fun fromID(id: String) = callbackFlow {
        val doc: DocumentReference = collection.document(id)

        val listener  = doc.addSnapshotListener { value, error ->
            error?.let { cancel(it.message.toString(), it) }
            if(value!!.exists())
                trySend(value.data?.fromDocument(value.id))
        }

        awaitClose { listener.remove() }
    }
    fun fromFirebase(user: FirebaseUser) = fromID(user.uid)
    fun fromDisplayName(displayName: String) = callbackFlow {
        val docs = collection.startAt(displayName).endAt(displayName + "\uf8ff")

        val listener = docs.addSnapshotListener { value, error ->
            error?.let { cancel(it.message.toString(), it) }
            trySend(value?.map { it.data.fromDocument(it.id) } ?: listOf<UserModel>())
        }

        awaitClose { listener.remove() }
    }
    fun fromActualName(actualName: String) = callbackFlow {
        val docs = collection.startAt(actualName).endAt(actualName + "\uf8ff")

        val listener = docs.addSnapshotListener { value, error ->
            error?.let { cancel(it.message.toString(), it) }
            trySend(value?.map { it.data.fromDocument(it.id) } ?: listOf<UserModel>())
        }

        awaitClose { listener.remove() }
    }

    suspend fun insert(user: UserModel) {
        collection.document(user.ID).set(user.toDocument()).await()
    }
    suspend fun update(user: UserModel) {
        collection.document(user.ID).update(user.toDocument() as Map<String, Any?>).await()
    }

    suspend fun delete(id: String) { collection.document(id).delete().await() }
    suspend fun delete(user: UserModel) = delete(user.ID)
}
