package uk.co.sksulai.multitasker.db.web

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.tasks.await

import uk.co.sksulai.multitasker.db.converter.DateConverter
import uk.co.sksulai.multitasker.db.converter.UriConverter
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.util.getAwait
import java.time.Instant

fun Timestamp.toInstance() = Instant.ofEpochSecond(seconds, nanoseconds.toLong())

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
        "Avatar"        to UriConverter().from(Avatar),
        "DOB"           to DateConverter().from(DOB),
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
        Avatar        = UriConverter().to(get("Avatar") as String?),
        DOB           = DateConverter().to(get("DOB") as String?),
        Home          = get("Home") as String?
    )

    suspend fun fromID(id: String): UserModel {
        val doc = collection.document(id).getAwait()
        return doc.data?.fromDocument(id)!!
    }
    suspend fun fromFirebase(user: FirebaseUser): UserModel = fromID(user.uid)
    suspend fun fromDisplayName(displayName: String): List<UserModel> {
        val docs = collection.startAt(displayName).endAt(displayName + "\uf8ff").getAwait()
        return docs.documents.map { it.data!!.fromDocument(it.id) }
    }
    suspend fun fromActualName(actualName: String): List<UserModel> {
        val docs = collection.startAt(actualName).endAt(actualName + "\uf8ff").getAwait()
        return docs.documents.map { it.data!!.fromDocument(it.id) }
    }

    suspend fun insert(user: UserModel) {
        collection.document(user.ID).set(user.toDocument()).await()
    }
    suspend fun update(user: UserModel) {
        collection.document(user.ID).update(user.toDocument() as Map<String, Any?>).await()
    }

    suspend fun delete(id: String) {
        collection.document(id).delete().await()
    }
    suspend fun delete(user: UserModel) = delete(user.ID)
}
