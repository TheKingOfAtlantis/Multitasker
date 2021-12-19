package uk.co.sksulai.multitasker.db.web

import javax.inject.Inject

import java.time.Instant

import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.firestore.*

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

import uk.co.sksulai.multitasker.db.converter.DateConverter
import uk.co.sksulai.multitasker.db.converter.UriConverter
import uk.co.sksulai.multitasker.db.datasource.UserDataSource
import uk.co.sksulai.multitasker.db.model.UserModel

fun Timestamp.toInstance(): Instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())

interface IUserWebService : UserDataSource, WebService

/**
 * Used to access the user documents in Firestore
 * @param db The Firestore database
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserWebService @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IUserWebService {
    /**
     * Root collection containing user data
     */
    private val collection = db.collection("users")
    private val currentUser get() = auth.currentUser

    /**
     * Helper to convert UserModels to Firestore documents
     */
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
    /**
     * Helper to convert Firestore documents to UserModels
     */
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

    /**
     * Retrieves users given an id
     * @param id The ID to be used for the query
     */
    override fun fromID(id: String) = callbackFlow {
        val doc: DocumentReference = collection.document(id)

        val listener = doc.addSnapshotListener { value, error ->
            error?.let { cancel(it.message.toString(), it) }
            if(value!!.exists())
                trySend(value.data?.fromDocument(value.id))
        }

        awaitClose { listener.remove() }
    }
    /**
     * Retrieves users given the Firebase user
     * @param user The user to be used for the query
     */
    fun fromFirebase(user: FirebaseUser) = fromID(user.uid)
    /**
     * Retrieves users given the display name to query
     * @param user The display name to be used for the query
     */
    override fun fromDisplayName(displayName: String) = callbackFlow {
        val docs = collection.startAt(displayName).endAt(displayName + "\uf8ff")

        val listener = docs.addSnapshotListener { value, error ->
            error?.let { cancel(it.message.toString(), it) }
            trySend(value?.map { it.data.fromDocument(it.id) } ?: listOf<UserModel>())
        }

        awaitClose { listener.remove() }
    }
    /**
     * Retrieves users given the actual name to query
     * @param user The actual name to be used for the query
     */
    override fun fromActualName(actualName: String) = callbackFlow {
        val docs = collection.startAt(actualName).endAt(actualName + "\uf8ff")

        val listener = docs.addSnapshotListener { value, error ->
            error?.let { cancel(it.message.toString(), it) }
            trySend(value?.map { it.data.fromDocument(it.id) } ?: listOf<UserModel>())
        }

        awaitClose { listener.remove() }
    }

    /**
     * Adds a user to the database
     * @param user The user to be added
     */
    override suspend fun insert(user: UserModel) {
        collection.document(user.ID).set(user.toDocument()).await()
    }
    /**
     * Updates a user to the database
     * @param user The updated user to be modify the database with
     */
    override suspend fun update(user: UserModel) {
        collection.document(user.ID).update(user.toDocument() as Map<String, Any?>).await()

        val requestBuilder = UserProfileChangeRequest.Builder()
        if(currentUser?.displayName != user.DisplayName) requestBuilder.displayName = user.DisplayName
        if(currentUser?.photoUrl != user.Avatar) requestBuilder.photoUri = user.Avatar
        currentUser?.updateProfile(requestBuilder.build())?.await()
    }

    /**
     * Deletes a user to the database
     * @param id The id of the user to be removed
     */
    suspend fun delete(id: String) {
        collection.document(id).delete().await()
        auth.currentUser?.delete()
    }
    /**
     * Deletes a user to the database
     * @param user The user to be removed
     */
    override suspend fun delete(user: UserModel) = delete(user.ID)
}
