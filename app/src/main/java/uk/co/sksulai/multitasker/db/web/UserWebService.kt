package uk.co.sksulai.multitasker.db.web

import org.jetbrains.annotations.TestOnly

import javax.inject.Inject

import java.time.Instant
import android.net.Uri

import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage

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
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
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
        "Email"         to email,
        "Creation"      to Timestamp(creation.epochSecond, creation.nano),
        "LastModified"  to Timestamp(lastModified.epochSecond, lastModified.nano),
        "DisplayName"   to displayName,
        "PreferredHome" to preferredHome,
        "ActualName"    to actualName,
        "Avatar"        to UriConverter.from(avatar),
        "DOB"           to DateConverter.from(dob),
        "Home"          to home
    )
    /**
     * Helper to convert Firestore documents to UserModels
     */
    private fun MutableMap<String, Any?>.fromDocument(id: String) = UserModel(
        userID            = id,
        creation      = (get("Creation") as Timestamp).toInstance(),
        lastModified  = (get("LastModified") as Timestamp).toInstance(),
        email         = get("Email") as String,
        displayName   = get("DisplayName") as String,
        preferredHome = get("PreferredHome") as String,
        actualName    = get("ActualName") as String?,
        avatar        = UriConverter.to(get("Avatar") as String?),
        dob           = DateConverter.to(get("DOB") as String?),
        home          = get("Home") as String?
    )

    @TestOnly override fun getAll() = callbackFlow {
        val listener = collection.addSnapshotListener { value, error ->
            error?.let { cancel(it.message ?: "", it) }
            trySend(value?.map { it.data.fromDocument(it.id) } ?: listOf<UserModel>())
        }
        awaitClose { listener.remove() }
    }

    suspend fun doesExist(id: String): Boolean {
        val doc = collection.document(id).get().await()
        return doc.exists()
    }

    /**
     * Retrieves users given an id
     * @param id The ID to be used for the query
     */
    override fun fromID(id: String) = callbackFlow {
        val doc: DocumentReference = collection.document(id)

        val listener = doc.addSnapshotListener { value, error ->
            error?.let { cancel(it.message ?: "", it) }
            trySend(value?.takeIf { it.exists() }?.data?.fromDocument(value.id))
        }

        awaitClose { listener.remove() }
    }
    /**
     * Retrieves users given the Firebase user
     * @param user The user to be used for the query
     */
    fun fromFirebase(user: FirebaseUser) = fromID(user.uid)

    fun CollectionReference.queryHandler(field: String, parameter: String): Query {
        var searchQuery: String = parameter

        val anyEnd = searchQuery.endsWith('%')
        if(anyEnd) searchQuery = searchQuery.removeSuffix("%")

        return when {
            anyEnd -> orderBy(field).startAt(searchQuery).endAt("$searchQuery\uf8ff")
            else -> whereEqualTo(field, searchQuery)
        }
    }

    /**
     * Retrieves users given the display name to query
     * @param user The display name to be used for the query
     */
    override fun fromDisplayName(displayName: String) = callbackFlow {
        val docs = collection.queryHandler("DisplayName", displayName)

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
        val docs = collection.queryHandler("ActualName", actualName)

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
        collection.document(user.userID).set(user.toDocument()).await()
    }
    /**
     * Updates a user to the database
     * @param user The updated user to be modify the database with
     */
    override suspend fun update(user: UserModel) {
        collection.document(user.userID).update(user.toDocument() as Map<String, Any?>).await()

        val requestBuilder = UserProfileChangeRequest.Builder()
        if(currentUser?.displayName != user.displayName) requestBuilder.displayName = user.displayName
        if(currentUser?.photoUrl != user.avatar) requestBuilder.photoUri = user.avatar
        currentUser?.updateProfile(requestBuilder.build())?.await()
    }

    /**
     * Uploads a profile picture for the specified user
     *
     * @param userId ID of the user
     * @param avatar Uri to the new picture
     *
     * @return The url to download the profile picture in future
     */
    suspend fun uploadAvatar(userId: String, avatar: Uri): Uri {
        val ref  = storage.getReference("$userId/profilePicture")
        ref.putFile(avatar).await()
        return ref.downloadUrl.await()
    }
    /**
     * Deletes the profile picture for the specified user
     * @param userId ID of the user
     */
    suspend fun deleteAvatar(userId: String) {
        val ref = storage.getReference("$userId/profilePicture")
        ref.delete().await()
    }

    /**
     * Deletes a user to the database
     * @param id The id of the user to be removed
     */
    suspend fun delete(id: String) {
        // Need to delete from Firestore database first while we still have w/r access
        collection.document(id).delete().await()

        // If we are deleting the current user then we need to delete authentication details
        if (auth.currentUser?.uid == id)
            auth.currentUser?.delete()?.await()
    }
    /**
     * Deletes a user to the database
     * @param user The user to be removed
     */
    override suspend fun delete(user: UserModel) = delete(user.userID)
}
