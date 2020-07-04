package uk.co.sksulai.multitasker.db.web

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import kotlinx.coroutines.tasks.await

import uk.co.sksulai.multitasker.db.converter.DateConverter
import uk.co.sksulai.multitasker.db.converter.UriConverter
import uk.co.sksulai.multitasker.db.model.UserModel
import uk.co.sksulai.multitasker.util.getAwait

class UserWebService {
    private val db = Firebase.firestore
    private val collection = db.collection("user")

    private fun UserModel.toDocument() = mutableMapOf(
        "ID"          to ID,
        "FirebaseID"  to FirebaseID,
        "DisplayName" to DisplayName,
        "ActualName"  to ActualName,
        "Avatar"      to UriConverter().from(Avatar),
        "Email"       to Email,
        "DOB"         to DateConverter().from(DOB),
        "Home"        to Home
    )
    private fun MutableMap<String, Any?>.fromDocument() = UserModel(
        ID          = get("ID") as String,
        FirebaseID  = get("FirebaseID") as String?,
        DisplayName = get("DisplayName") as String?,
        ActualName  = get("ActualName") as String?,
        Avatar      = UriConverter().to(get("Avatar") as String?),
        Email       = get("Email") as String?,
        DOB         = DateConverter().to(get("DOB") as String?),
        Home        = get("Home") as String?
    )

    suspend fun fromID(id: String): UserModel? {
        val doc = collection.document(id).getAwait()
        return doc.data?.fromDocument()
    }
    suspend fun fromFirebase(user: FirebaseUser): UserModel? {
        val docs = collection.whereEqualTo("FirebaseID", user.uid).getAwait()
        return docs.single().data.fromDocument()
    }
    suspend fun fromDisplayName(displayName: String): List<UserModel> {
        val docs = collection.startAt(displayName).endAt(displayName + "\uf8ff").getAwait()
        return docs.documents.map { it.data!!.fromDocument() }
    }
    suspend fun fromActualName(actualName: String): List<UserModel> {
        val docs = collection.startAt(actualName).endAt(actualName + "\uf8ff").getAwait()
        return docs.documents.map { it.data!!.fromDocument() }
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
